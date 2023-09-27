package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID
import squants.market.Money
import shop.domain.brand._
import shop.domain.category._
import shop.optics._
import derevo.derive
import derevo.circe.magnolia.decoder
import derevo.circe.magnolia.encoder
import derevo.circe.magnolia.keyDecoder
import derevo.circe.magnolia.keyEncoder
import derevo.cats.eqv
import derevo.cats.show

object item {
  @derive(decoder, encoder, keyDecoder, keyEncoder, eqv, show, uuid)
  @newtype
  case class ItemId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype
  case class ItemName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype
  case class ItemDescription(value: String)
  
  @derive(decoder, encoder, eqv, show)
  case class Item(
      uuid: ItemId,
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brand: Brand,
      category: Category
  )

  case class CreateItem(
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brandId: BrandId,
      categoryId: CategoryId
  )

  @derive(decoder, encoder)
  case class UpdateItem(
      id: ItemId,
      price: Money
  )
}
