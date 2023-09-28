package shop.http.routes

import shop.services.HealthCheck
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import cats.Monad
import org.http4s.server.Router

final case class HealthRoutes[F[_]: Monad](
    healthcheck: HealthCheck[F]
) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/healthcheck"
  private val httpRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(healthcheck.status)
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoute
  )
}
