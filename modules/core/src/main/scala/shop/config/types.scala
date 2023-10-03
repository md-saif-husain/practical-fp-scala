package shop.config

import shop.ext.ciris._

import ciris._
import ciris.refined._
import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration
import derevo.derive
import derevo.cats.show
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString

object types {

  @newtype case class ShoppingCartExpiration(value: FiniteDuration)
  @newtype case class TokenExpiration(value: FiniteDuration)

  @derive(configDecoder, show)
  @newtype
  case class JwtAccessTokenKeyConfig(secret: NonEmptyString)

  @derive(configDecoder, show)
  @newtype
  case class PasswordSalt(secret: NonEmptyString)

}
