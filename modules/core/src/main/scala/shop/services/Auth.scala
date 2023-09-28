package shop.services

import shop.domain.auth._
import pdi.jwt.JwtClaim
import dev.profunktor.auth.jwt.JwtToken

trait Auth[F[_]] {
    def newUser(username: UserName, password: Password): F[JwtToken]
    def login(username: UserName, password: Password): F[JwtToken]
    def logout(token: JwtToken, username: UserName): F[Unit]
}

trait UserAuth[F[_], A] {
    def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}