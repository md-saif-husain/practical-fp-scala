package shop.domain

import shop.domain.auth._
import squants.market.Money
import shop.domain.checkout._

object payment {
  case class Payment(
      id: UserId,
      total: Money,
      card: Card
  )
}
