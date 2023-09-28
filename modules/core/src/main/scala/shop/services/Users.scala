package shop.services

import shop.domain.auth._
import shop.http.auth.users
trait Users[F[_]] {
  def find(username: UserName): F[Option[users.UserWithPassword]]
  def create(username: UserName, password: EncryptedPassword): F[UserId]
}