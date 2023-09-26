package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID

object brand {
    @newtype case class BrandId(value: UUID)
    @newtype case class BrandName(value: String)
    
    case class Brand(uuid: BrandId, name: BrandName)

}

