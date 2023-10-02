package shop

import java.util.UUID

import shop.domain.brand._
import shop.domain.category._
import shop.domain.cart._
import shop.domain.item._
import shop.domain.checkout._
import shop.domain.auth._
import shop.domain.order._
import shop.http.auth.users._
import shop.domain.payment._

import org.scalacheck.Gen
import cats.syntax.all._
import squants.market.Money
import squants.market.USD
import eu.timepit.refined.api.Refined


object generator {
  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)

      }

  def nesGen[A](f: String => A): Gen[A] =
    nonEmptyStringGen.map(f)

  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  //<--------- Domain Generator ----------------------
  val BrandIdGen: Gen[BrandId]     = idGen(BrandId.apply)
  val BrandNameGen: Gen[BrandName] = nesGen(BrandName.apply)

  val CategoryIdGen: Gen[CategoryId]     = idGen(CategoryId.apply)
  val CategoryNameGen: Gen[CategoryName] = nesGen(CategoryName.apply)

  val ItemIdGen: Gen[ItemId]                   = idGen(ItemId.apply)
  val ItemNameGen: Gen[ItemName]               = nesGen(ItemName.apply)
  val ItemDescriptionGen: Gen[ItemDescription] = nesGen(ItemDescription.apply)

  val userIdGen: Gen[UserId] = idGen(UserId.apply)

  val orderIdGen: Gen[OrderId] = idGen(OrderId.apply)

  val paymentIdGen: Gen[PaymentId] = idGen(PaymentId.apply)

  val userNameGen: Gen[UserName] = nesGen(UserName.apply)

  val passwordGen: Gen[Password] = nesGen(Password.apply)

  val encryptedPasswordGen: Gen[EncryptedPassword] = nesGen(EncryptedPassword.apply)

  val quantityGen: Gen[Quantity] = Gen.posNum[Int].map(Quantity.apply)

  val cardNameGen: Gen[CardName] =
    Gen
      .stringOf(
        Gen.oneOf(('a' to 'z') ++ ('A' to 'Z'))
      )
      .map { x =>
        CardName(Refined.unsafeApply(x))

      }

  val brandGen: Gen[Brand] =
    for {
      i <- BrandIdGen
      n <- BrandNameGen
    } yield Brand(i, n)

  val categoryGen: Gen[Category] =
    for {
      i <- CategoryIdGen
      n <- CategoryNameGen
    } yield Category(i, n)

  val moneyGen: Gen[Money] =
    Gen.posNum[Long].map { n =>
      USD(n)
    }

  val itemGen: Gen[Item] =
    for {
      i <- ItemIdGen
      n <- ItemNameGen
      d <- ItemDescriptionGen
      m <- moneyGen
      b <- brandGen
      c <- categoryGen

    } yield Item(i, n, d, m, b, c)

  val cartItemGen: Gen[CartItem] =
    for {
      i <- itemGen
      q <- quantityGen
    } yield CartItem(i, q)

  val cartTotalGen: Gen[CartTotal] =
    for {
      i <- Gen.nonEmptyListOf(cartItemGen)
      m <- moneyGen
    } yield CartTotal(i, m)

  val itemMapGen: Gen[(ItemId, Quantity)] =
    for {
      i <- ItemIdGen
      q <- quantityGen
    } yield i -> q

  val cartGen: Gen[Cart] = Gen.nonEmptyMap(itemMapGen).map(Cart.apply)

  // Generator for Refinement types
  private def sized(size: Int): Gen[Long] = {
    def go(s: Int, acc: String): Gen[Long] =
      Gen.oneOf(1 to 9).flatMap { n =>
        if (s === size) acc.toLong
        else go(s + 1, acc + n.toString)
      }

    go(0, "")
  }

  val cardGen: Gen[Card] =
    for {
      n <- cardNameGen
      u <- sized(16).map(x => CardNumber(Refined.unsafeApply(x)))
      x <- sized(4).map(x => CardExpiration(Refined.unsafeApply(x.toString)))
      c <- sized(3).map(x => CardCVV(Refined.unsafeApply(x.toInt)))
    } yield Card(n, u, x, c)

  //---------------Http Routes ------------------------

  val userGen: Gen[User] = 
    for {
        i <- userIdGen
        n <- userNameGen
    } yield User(i, n)

  val commonUserGen: Gen[CommonUser] = userGen.map(CommonUser(_))  

  val adminUserGen: Gen[AdminUser] = userGen.map(AdminUser(_))

  val paymentGen: Gen[Payment] = 
    for {
        i <- userIdGen
        t <- moneyGen
        c <- cardGen
    } yield Payment(i, t, c)
}
