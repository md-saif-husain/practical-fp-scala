package shop.domain

import io.estatico.newtype.macros.newtype
import shop.domain.item._
import squants.market.Money

object cart {
  @newtype case class Quantity(value: Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])

  case class CartItem(item: Item, quantity: Quantity)
  case class CartTotal(items: List[CartItem], total: Money)

}
