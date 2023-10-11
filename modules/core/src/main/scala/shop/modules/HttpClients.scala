package shop.modules

import shop.http.clients.PaymentClient
import shop.config.types
import org.http4s.client.Client
import org.http4s.circe.JsonDecoder
import cats.effect.MonadCancelThrow

trait HttpClients[F[_]] {
  def payment: PaymentClient[F]
}

object HttpClients {
  def make[F[_]: JsonDecoder: MonadCancelThrow](
      cfg: types.PaymentConfig,
      client: Client[F]
  ): HttpClients[F] =
    new HttpClients[F] {
      def payment: PaymentClient[F] = PaymentClient.make[F](cfg, client)
    }
}
