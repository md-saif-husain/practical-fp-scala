package shop.http.routes.secured

import shop.domain.cart._
import shop.domain.item._
import shop.domain.auth._
import shop.http.auth.users._
import shop.generator._
import suite.HttpSuite
import shop.services.ShoppingCart
import shop.http.routes.secured.CartRoutes

import cats._
import cats.implicits._
import cats.syntax.all._
import cats.data.Kleisli
import cats.effect._
import org.scalacheck.Gen
import org.http4s.Method._
import org.http4s._
import org.http4s.server._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.http4s.circe.CirceEntityEncoder._
import squants.market.USD

object CartRoutesSuite extends HttpSuite {
  // fake auth Middleware
  def authMiddleware(authUser: CommonUser): AuthMiddleware[IO, CommonUser] =
    AuthMiddleware(Kleisli.pure(authUser))

  def dataCart(cart: CartTotal) = new TestShoppingCart {
    override def get(userId: UserId): IO[CartTotal] = IO.pure(cart)
  }

  test("Get carts succeed") {
    val gen = for {
      u  <- commonUserGen
      ct <- cartTotalGen
    } yield (u, ct)
    forall(gen) {
      case (u, ct) =>
        val req    = GET(uri"/carts")
        val routes = CartRoutes[IO](dataCart(ct)).routes(authMiddleware(u))
        expectHttpBodyAndStatus(routes, req)(ct, Status.Ok)
    }
  }

  test("POST add item to shopping cart succeed") {
    val gen = for {
      u <- commonUserGen
      c <- cartGen
    } yield u -> c

    forall(gen) {
      case (user, c) =>
        val req    = POST(c, uri"/carts")
        val routes = CartRoutes[IO](new TestShoppingCart).routes(authMiddleware(user))
        expectHttpStatus(routes, req)(Status.Created)
    }
  }

  protected class TestShoppingCart extends ShoppingCart[IO] {
    override def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = IO.unit
    override def get(userId: UserId): IO[CartTotal]                                = IO.pure(CartTotal(List.empty, USD(0)))
    override def delete(userId: UserId): IO[Unit]                                  = IO.unit
    override def removeItem(userId: UserId, itemId: ItemId): IO[Unit]              = IO.unit
    override def update(userId: UserId, cart: Cart): IO[Unit]                      = IO.unit

  }
}
