package shop.http.routes.auth

import shop.services.Auth
import shop.http.auth.users.CommonUser

import cats.Monad
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import dev.profunktor.auth.AuthHeaders

final case class LogoutRoutes[F[_]: Monad](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private[routes] val pathPrefix = "/auth"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse(auth.logout(_, user.value.name)) *> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
