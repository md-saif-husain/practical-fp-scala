package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID
import shop.domain.item._
import shop.domain.cart._

object order {
  @newtype case class OrderId(uuid: UUID)
  @newtype case class PaymentId(uuid: UUID)

  case class Order(
      id: OrderId,
      pid: PaymentId,
      items: Map[Item, Quantity]
  )
}
