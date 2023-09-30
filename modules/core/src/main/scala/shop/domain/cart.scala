package shop.domain

import io.estatico.newtype.macros.newtype
import shop.domain.item._
import squants.market.Money
import derevo.derive
import derevo.circe.magnolia.decoder
import derevo.circe.magnolia.encoder
import derevo.cats._
import io.circe.Encoder
import io.circe.Decoder
import squants.market.USD

object cart {
  @derive(decoder, encoder, eqv, show)
  @newtype
  case class Quantity(value: Int)

  @derive(eqv, show)
  @newtype
  case class Cart(items: Map[ItemId, Quantity])

  object Cart {
    implicit val jsonEncoder: Encoder[Cart] =
      Encoder.forProduct1("items")(_.items)

    implicit val jsonDecoder: Decoder[Cart] =
      Decoder.forProduct1("items")(Cart.apply)
  }

  @derive(decoder, encoder, eqv, show)
  case class CartItem(item: Item, quantity: Quantity) {
    def subTotal: Money = USD(item.price.amount * quantity.value)
  }

  @derive(decoder, encoder, eqv, show)
  case class CartTotal(items: List[CartItem], total: Money)

}