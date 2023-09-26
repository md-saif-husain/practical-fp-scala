package shop.services

import shop.domain.order._
import shop.domain.auth.UserId
import shop.domain.cart.CartItem

import squants.market.Money
import cats.data.NonEmptyList

trait Orders[F[_]] {
  def get(userId: UserId, orderId: OrderId): F[Option[Order]]
  def findBy(userId: UserId): F[List[Order]]
  def create(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId]
}