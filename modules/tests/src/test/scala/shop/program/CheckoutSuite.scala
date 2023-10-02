package shop.program

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

import shop.domain.auth._
import shop.domain.cart._
import shop.domain.item._
import shop.domain.order._
import shop.domain.payment._
import shop.effects.TestBackground
import shop.generator._
import shop.http.clients._
import shop.retries.TestRetry
import shop.services._
import shop.programs.Checkout

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.kernel.Ref
import cats.implicits._
import org.typelevel.log4cats.noop.NoOpLogger
import retry.RetryDetails._
import retry.RetryPolicies._
import retry.RetryPolicy
import squants.market._
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object CheckoutSuite extends SimpleIOSuite with Checkers {
  val MaxRetries = 3

  val retryPolicy: RetryPolicy[IO] = limitRetries[IO](MaxRetries)

  def succesfulClient(id: PaymentId): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] = IO.pure(id)
    }

  def unReachableClient(id: PaymentId): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] = IO.raiseError(PaymentError(""))
    }

  def recoveringClient(
      attemptSoFar: Ref[IO, Int],
      paymentId: PaymentId
  ): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] =
        attemptSoFar.get.flatMap {
          case n if n === 1 => IO.pure(paymentId)
          case _            => attemptSoFar.update(_ + 1) *> IO.raiseError(PaymentError(""))
        }
    }

  def succesfulCart(cartTotal: CartTotal): ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(cartTotal)
      override def delete(userId: UserId): IO[Unit]   = IO.unit
    }

  def emptyCart(cartTotal: CartTotal): ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(CartTotal(List.empty, USD(0)))
    }

  def failingCart(cartTotal: CartTotal): ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(cartTotal)
      override def delete(userId: UserId): IO[Unit]   = IO.raiseError(new NoStackTrace {})
    }

  def succesfulOrder(oid: OrderId): Orders[IO] =
    new TestOrder {
      override def create(
          userId: UserId,
          paymentId: PaymentId,
          items: NonEmptyList[CartItem],
          total: Money
      ): IO[OrderId] =
        IO.pure(oid)
    }
  def failingOrders: Orders[IO] = new TestOrder {
    override def create(
        userId: UserId,
        paymentId: PaymentId,
        items: NonEmptyList[CartItem],
        total: Money
    ): IO[OrderId] = IO.raiseError(OrderError(""))
  }

  implicit val bg = TestBackground.NoOp
  implicit val lg = NoOpLogger[IO]
  val gen = for {
    uid <- userIdGen
    pid <- paymentIdGen
    oid <- orderIdGen
    crt <- cartTotalGen
    crd <- cardGen
  } yield (uid, pid, oid, crt, crd)

  test("successful checkout") {
    forall(gen) {
      case (uid, pid, oid, ct, card) =>
        Checkout[IO](
          succesfulClient(pid),
          succesfulCart(ct),
          succesfulOrder(oid),
          retryPolicy
        ).process(uid, card).map(expect.same(oid, _))
    }
  }

  test("empty cart") {
    forall(gen) {
      case (uid, pid, oid, ct, card) =>
        Checkout[IO](
          succesfulClient(pid),
          emptyCart(ct),
          succesfulOrder(oid),
          retryPolicy
        ).process(uid, card)
          .attempt
          .map {
            case Left(EmptyCartError) => success
            case _                    => failure("Cart was not empty as expected")
          }
    }
  }

  test("unreachable client") {
    forall(gen) {
      case (uid, pid, oid, ct, card) =>
        Ref.of[IO, Option[GivingUp]](None).flatMap { retries =>
          implicit val rh = TestRetry.givingUp(retries)
          Checkout[IO](
            unReachableClient(pid),
            succesfulCart(ct),
            succesfulOrder(oid),
            retryPolicy
          ).process(uid, card)
            .attempt
            .flatMap {
              case Left(PaymentError(_)) =>
                retries.get.map {
                  case Some(g) =>
                    expect.same(g.totalRetries, MaxRetries)
                  case None =>
                    failure("expected GivingUp")
                }
              case _ =>
                IO.pure(failure("Expected Payment Error"))
            }
        }
    }
  }

  test("failing payment client succeeds after one retry") {
    forall(gen) {
      case (uid, pid, oid, ct, card) =>
        (Ref.of[IO, Option[WillDelayAndRetry]](None), Ref.of[IO, Int](0)).tupled.flatMap {
          case (retries, cliRef) =>
            implicit val lh = TestRetry.recovering(retries)
            Checkout[IO](
              recoveringClient(attemptSoFar = cliRef, pid),
              succesfulCart(ct),
              succesfulOrder(oid),
              retryPolicy
            ).process(uid, card)
              .attempt
              .flatMap {
                case Right(id) =>
                  retries.get.map {
                    case Some(w) => expect.same(id, oid) |+| expect.same(0, w.retriesSoFar)
                    case _       => failure("Expected one retry")
                  }
                case Left(_) => IO.pure(failure("Expected Payment Id"))
              }
        }
    }
  }

  test("cannot create order run in background") {
    forall(gen) {
      case (uid, pid, _, ct, card) =>
        (
          Ref.of[IO, (Int, FiniteDuration)](0 -> 0.second),
          Ref.of[IO, Option[GivingUp]](None)
        ).tupled.flatMap {
          case (acc, retries) =>
            implicit val bg = TestBackground.counter(acc)
            implicit val rh = TestRetry.givingUp(retries)
            Checkout[F](
              succesfulClient(pid),
              succesfulCart(ct),
              failingOrders,
              retryPolicy
            ).process(uid, card)
              .attempt
              .flatMap {
                case Left(OrderError(_)) =>
                  (acc.get, retries.get).mapN {
                    case (c, Some(g)) =>
                      expect.same(c, 1 -> 1.hour) |+|
                        expect.same(g.totalRetries, MaxRetries)
                    case _ =>
                      failure(s"Expected $MaxRetries retries and schedules")
                  }
                case _ => IO.pure(failure("Expected Order Error"))
              }
        }
    }
  }

  test("failing to delete cart does not affect checkout") {
    forall(gen) {
      case (uid, pid, oid, ct, card) =>
        Checkout[IO](
          succesfulClient(pid),
          failingCart(ct),
          succesfulOrder(oid),
          retryPolicy
        ).process(uid, card)
          .map(expect.same(oid, _))
    }
  }
}

protected class TestCart() extends ShoppingCart[IO] {
  override def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
  override def get(userId: UserId): IO[CartTotal]                                = ???
  override def delete(userId: UserId): IO[Unit]                                  = ???
  override def removeItem(userId: UserId, itemId: ItemId): IO[Unit]              = ???
  override def update(userId: UserId, cart: Cart): IO[Unit]                      = ???

}

protected class TestOrder() extends Orders[IO] {
  override def get(userId: UserId, orderId: OrderId): IO[Option[Order]] = ???
  override def findBy(userId: UserId): IO[List[Order]]                  = ???
  override def create(userId: UserId, paymentId: PaymentId, items: NonEmptyList[CartItem], total: Money): IO[OrderId] =
    ???
}
