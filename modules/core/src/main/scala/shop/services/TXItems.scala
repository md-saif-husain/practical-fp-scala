package shop.services

import shop.domain.item.ItemId
import shop.domain.brand.BrandName
import shop.domain.category.CategoryName
import shop.domain.item._

import squants.market.Money

// Example of transactions, not used in the application
trait TxItems[F[_]] {
  def create(item: TxItems.ItemCreation): F[ItemId]
}

object TxItems {

  case class ItemCreation(
      brand: BrandName,
      category: CategoryName,
      name: ItemName,
      description: ItemDescription,
      price: Money
  )

}
