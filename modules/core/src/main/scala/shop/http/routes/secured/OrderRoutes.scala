package shop.http.routes.secured

import shop.services.Orders
import shop.http.auth.users._
import shop.http.vars._

import cats.Monad
import cats.syntax.all._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final case class OrderRoutes[F[_]: Monad](
    order: Orders[F]
) extends Http4sDsl[F] {

  private[routes] val pathPrefix = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user =>
      Ok(order.findBy(user.value.id))

    case GET -> Root / OrderIdVar(orderId) as user =>
      Ok(order.get(user.value.id, orderId))
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
