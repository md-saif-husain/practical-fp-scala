package shop.config

import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration

object types {

  @newtype case class ShoppingCartExpiration(value: FiniteDuration)

}
