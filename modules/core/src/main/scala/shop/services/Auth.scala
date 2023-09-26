package shop.services

import shop.domain.auth._
import pdi.jwt.JwtClaim

trait Auth[F[_]] {
    def newUser(username: UserName, password: Password): F[JWTToken]
    def login(username: UserName, password: Password): F[JWTToken]
    def logout(token: JWTToken, username: UserName): F[Unit]
}

trait UserAuth[F[_], A] {
    def findUser(token: JWTToken)(claim: JwtClaim): F[Option[A]]
}