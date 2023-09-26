package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID

object auth {
  @newtype case class UserId(uuid: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)
  @newtype case class EncryptedPassword(value: String)
  @newtype case class JWTToken(value: String)

  case class UserWithPassword(
      id: UserId,
      name: UserName,
      password: EncryptedPassword
  )

  case class User(id: UserId, name: UserName)
}
