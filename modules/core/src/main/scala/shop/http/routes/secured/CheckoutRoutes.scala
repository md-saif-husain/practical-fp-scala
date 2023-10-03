package shop.http.routes.secured

import shop.programs.Checkout
import shop.domain.checkout._
import shop.domain.cart._
import shop.domain.order._
import shop.http.auth.users._
import shop.ext.http4s.refined._

import cats.MonadThrow
import cats.syntax.all._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.JsonDecoder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final case class CheckoutRoutes[F[_]: JsonDecoder: MonadThrow](
    checkout: Checkout[F]
) extends Http4sDsl[F] {
  private[routes] val pathPrefix = "/checkout"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root as user =>
      ar.req.decodeR[Card] { card =>
        checkout
          .process(user.value.id, card)
          .flatMap(Created(_))
          .recoverWith {
            case CartNotFound(userId) =>
              NotFound(s"cart not found for user ${userId.value}")
            case EmptyCartError =>
              BadRequest(s"Shopping cart is empty")
            case e: OrderOrPaymentError =>
              BadRequest(e.show)
          }
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
