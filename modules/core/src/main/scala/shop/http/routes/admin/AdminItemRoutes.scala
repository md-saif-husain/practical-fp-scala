package shop.http.routes.admin

import shop.services.Items
import shop.http.auth.users._
import shop.ext.http4s.refined._
import shop.domain.item._

import cats.MonadThrow
import cats.syntax.all._
import io.circe.JsonObject
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final case class AdminItemRoutes[F[_]: JsonDecoder: MonadThrow](
    items: Items[F]
) extends Http4sDsl[F] {
  private[admin] val pathPrefix = "/admin"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CreateItemParam] { item: CreateItemParam =>
          items.create(item.toDomain).flatMap { id =>
            Created(JsonObject.singleton("item_id", id.asJson))

          }

        }
      case ar @ PUT -> Root as _ =>
        ar.req.decodeR[UpdateItemParam] { item: UpdateItemParam =>
          items.update(item.toDomain) >> Ok()

        }
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
