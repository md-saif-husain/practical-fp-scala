package shop

import io.circe.Decoder
import squants.market.Money
import squants.market.USD
import io.circe.Encoder
import cats.kernel.Monoid
import cats.kernel.Eq
import cats.Show
import squants.market.Currency

package object domain extends OrphanInstances

// instance for types we do not construct
trait OrphanInstances {
  implicit val moneyDecoder: Decoder[Money] = Decoder[BigDecimal].map(USD.apply)
  implicit val moneyEncoder: Encoder[Money] = Encoder[BigDecimal].contramap(_.amount)
  implicit val moneyMonoid: Monoid[Money] = new Monoid[Money] {
    def empty: Money                       = USD(0)
    def combine(x: Money, y: Money): Money = x + y
  }
  implicit val currencyEq: Eq[Currency] = Eq.and(Eq.and(Eq.by(_.code), Eq.by(_.symbol)), Eq.by(_.name))

  implicit val moneyEq: Eq[Money] = Eq.and(Eq.by(_.amount), Eq.by(_.currency))

  implicit val moneyShow: Show[Money] = Show.fromToString

}
