package shop.http.routes.admin

import shop.services.Brands
import shop.http.auth.users._
import shop.ext.http4s.refined._
import shop.domain.brand._

import cats.MonadThrow
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.dsl.Http4sDsl
import io.circe.JsonObject
import io.circe.syntax._

final case class AdminBrandRoutes[F[_]: JsonDecoder: MonadThrow](
    brand: Brands[F]
) extends Http4sDsl[F] {
  private[routes] val pathPrefix = "/brands"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[BrandParam] { bp: BrandParam =>
          brand.create(bp.toDomain).flatMap { id =>
            Created(JsonObject.singleton("brand_id", id.asJson))
          }
        }
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
