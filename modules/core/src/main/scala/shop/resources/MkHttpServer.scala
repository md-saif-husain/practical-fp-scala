package shop.resources

import shop.config.types
import org.http4s.HttpApp
import cats.effect.kernel.{ Async, Resource }
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger
import org.http4s.ember.server.EmberServerBuilder

trait MkHttpServer[F[_]] {
  def newEmber(cfg: types.HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]
}

object MkHttpServer {
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = implicitly

  private def showEmberBanerr[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  implicit def forAsyncLogger[F[_]: Async: Logger]: MkHttpServer[F] =
    new MkHttpServer[F] {
      def newEmber(cfg: types.HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
        EmberServerBuilder
          .default[F]
          .withHost(cfg.host)
          .withPort(cfg.port)
          .withHttpApp(httpApp)
          .build
          .evalTap(showEmberBanerr[F])
    }
}
