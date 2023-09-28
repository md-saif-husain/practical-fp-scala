package shop.http.auth

import io.estatico.newtype.macros.newtype
import derevo.derive
import shop.domain.auth._
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import derevo.circe.magnolia.decoder
import derevo.circe.magnolia.encoder
import derevo.cats.show

object users {

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @derive(decoder, encoder, show)
  case class User(id: UserId, name: UserName)

  @derive(decoder, encoder)
  case class UserWithPassword(id: UserId, name: UserName, password: EncryptedPassword)

  @derive(show)
  @newtype
  case class CommonUser(value: User)

  @derive(show)
  @newtype
  case class AdminUser(value: User)

}