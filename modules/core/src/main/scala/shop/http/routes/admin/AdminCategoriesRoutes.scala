package shop.http.routes.admin

import shop.services.Categories
import shop.http.auth.users._
import shop.ext.http4s.refined._
import shop.domain.category._

import cats.MonadThrow
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.dsl.Http4sDsl
import io.circe.JsonObject
import io.circe.syntax._

final case class AdminCategoriesRoutes[F[_]: JsonDecoder: MonadThrow](
    categories: Categories[F]
) extends Http4sDsl[F] {
  private[admin] val pathPrefix = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CategoryParam] { c: CategoryParam =>
          categories.create(c.toDomain).flatMap { id =>
            Created(JsonObject.singleton("category_id", id.asJson))
          }
        }
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
