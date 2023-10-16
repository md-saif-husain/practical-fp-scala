package shop.config

import enumeratum.EnumEntry._
import enumeratum._

sealed abstract class AppEnviornment extends EnumEntry with Lowercase

object AppEnviornment extends Enum[AppEnviornment] with CirisEnum[AppEnviornment] {
  case object Test extends AppEnviornment
  case object Prod extends AppEnviornment

  val values = findValues
}
