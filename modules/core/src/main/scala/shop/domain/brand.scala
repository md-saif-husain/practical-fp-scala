package shop.domain

import shop.optics._

import io.estatico.newtype.macros.newtype
import java.util.UUID
import derevo.cats._
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import scala.util.control.NoStackTrace


object brand {
  @derive(decoder, encoder, eqv, show, uuid)
  @newtype 
  case class BrandId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype 
  case class BrandName(value: String)

  @derive(decoder, encoder, eqv, show)
  case class Brand(uuid: BrandId, name: BrandName)

  @derive(decoder, encoder)
  case class InvalidBrand(value: String) extends NoStackTrace

}
