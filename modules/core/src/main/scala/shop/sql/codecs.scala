package shop.sql

import shop.domain.brand._
import shop.domain.category._
import shop.domain.item._

import skunk.Codec
import skunk.codec.all._
import squants.market._

object codecs {
  val brandId: Codec[BrandId]     = uuid.imap[BrandId](BrandId(_))(_.value)
  val brandName: Codec[BrandName] = varchar.imap[BrandName](BrandName(_))(_.value)

  val categoryId: Codec[CategoryId]     = uuid.imap[CategoryId](CategoryId(_))(_.value)
  val categoryName: Codec[CategoryName] = varchar.imap[CategoryName](CategoryName(_))(_.value)

  val itemId: Codec[ItemId]            = uuid.imap[ItemId](ItemId(_))(_.value)
  val itemName: Codec[ItemName]        = varchar.imap[ItemName](ItemName(_))(_.value)
  val itemDesc: Codec[ItemDescription] = varchar.imap[ItemDescription](ItemDescription(_))(_.value)

  val money: Codec[Money] = numeric.imap[Money](USD(_))(_.amount)
}
