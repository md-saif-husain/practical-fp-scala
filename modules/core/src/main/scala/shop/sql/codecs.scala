package shop.sql

import shop.domain.brand._
import shop.domain.category._


import skunk.Codec
import skunk.codec.all._

object codecs {
    val brandId: Codec[BrandId] = uuid.imap[BrandId](BrandId(_))(_.value)
    val brandName: Codec[BrandName] = varchar.imap[BrandName](BrandName(_))(_.value)
    val categoryId: Codec[CategoryId] = uuid.imap[CategoryId](CategoryId(_))(_.value)
    val categoryName: Codec[CategoryName] = varchar.imap[CategoryName](CategoryName(_))(_.value)
}