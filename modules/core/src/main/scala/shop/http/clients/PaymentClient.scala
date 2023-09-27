package shop.http.clients

import shop.domain.payment._
import shop.domain.order._

trait PaymentClient[F[_]] {
    def process(payment: Payment): F[PaymentId]
}