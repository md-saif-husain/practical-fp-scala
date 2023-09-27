package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID
import derevo.cats._
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import shop.optics.uuid

object auth {
  @derive(decoder, encoder, eqv, show, uuid)
  @newtype 
  case class UserId(uuid: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype 
  case class UserName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype 
  case class Password(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype 
  case class EncryptedPassword(value: String)

  @newtype 
  case class JWTToken(value: String)

  case class UserWithPassword(
      id: UserId,
      name: UserName,
      password: EncryptedPassword
  )

  case class User(id: UserId, name: UserName)
}
