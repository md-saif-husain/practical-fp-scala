package shop.sql
import shop.domain.brand._

import skunk.Codec
import skunk.codec.all._

object codecs {
    val brandId: Codec[BrandId] = uuid.imap[BrandId](BrandId(_))(_.value)
    val brandName: Codec[BrandName] = varchar.imap[BrandName](BrandName(_))(_.value)
}