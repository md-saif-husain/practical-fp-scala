package shop.services

//import shop.auth.{ Crypto, Tokens }
import shop.config.types.TokenExpiration
import shop.domain._
import shop.domain.auth._
import shop.http.auth.users._
import shop.auth._

import cats._
import cats.syntax.all._
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode
import io.circe.syntax._
import pdi.jwt.JwtClaim

trait Auth[F[_]] {
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

trait UserAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object UserAuth {

  // Interpreter for AdminUser
  def admin[F[_]: Applicative](
      adminToken: JwtToken,
      adminUser: AdminUser
  ): UserAuth[F, AdminUser] =
    new UserAuth[F, AdminUser] {

      override def findUser(token: JwtToken)(claim: JwtClaim): F[Option[AdminUser]] =
        (token === adminToken)
          .guard[Option]
          .as(adminUser)
          .pure[F]
    }

  // Interpreter for CommonUser
  def common[F[_]: Functor](
      redis: RedisCommands[F, String, String]
  ): UserAuth[F, CommonUser] =
    new UserAuth[F, CommonUser] {

      override def findUser(token: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] =
        redis
          .get(token.value)
          .map {
            _.flatMap { u =>
              decode[User](u).toOption.map(CommonUser.apply)
            }
          }
    }
}

object Auth {
  def make[F[_]: MonadThrow](
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      users: Users[F],
      redis: RedisCommands[F, String, String],
      crypto: Crypto
  ): Auth[F] =
    new Auth[F] {
      private val TokenExpiration = tokenExpiration.value

      override def newUser(username: UserName, password: Password): F[JwtToken] =
        users.find(username = username).flatMap {
          case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
          case None =>
            for {
              i <- users.create(username, crypto.encrypt(password))
              t <- tokens.create
              u = User(i, username).asJson.noSpaces
              _ <- redis.setEx(t.value, u, TokenExpiration)
              _ <- redis.setEx(username.show, t.value, TokenExpiration)
            } yield t
        }

      override def login(username: UserName, password: Password): F[JwtToken] =
        users.find(username).flatMap {
          case None => UserNotFound(username).raiseError[F, JwtToken]
          case Some(user) if user.password =!= crypto.encrypt(password) =>
            InvalidPassword(username).raiseError[F, JwtToken]
          case Some(user) =>
            redis.get(username.show).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.setEx(
                    t.value,
                    user.asJson.noSpaces,
                    TokenExpiration
                  ) *> redis.setEx(username.show, t.value, TokenExpiration)
                }
            }
        }

      override def logout(token: JwtToken, username: UserName): F[Unit] =
        redis.del(token.show) *> redis.del(username.show).void

    }
}
