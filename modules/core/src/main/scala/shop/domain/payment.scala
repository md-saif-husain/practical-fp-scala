package shop.domain

import shop.domain.auth._
import shop.domain.checkout._

import derevo.cats._
import derevo.circe.magnolia.encoder
import derevo.derive
import squants.market.Money

object payment {
  @derive(encoder, show)
  case class Payment(
      id: UserId,
      total: Money,
      card: Card
  )
}
