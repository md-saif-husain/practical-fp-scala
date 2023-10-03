package shop.http

import java.util.UUID

import shop.domain.item.ItemId
import shop.domain.order.OrderId

import cats.implicits._

// For Http Request Param Types
object vars {
  protected class UUIDVar[A](f: UUID => A) {
    def unapply(str: String): Option[A] =
      Either.catchNonFatal(f(UUID.fromString(str))).toOption
  }

  object ItemIdVar  extends UUIDVar[ItemId](ItemId.apply)
  object OrderIdVar extends UUIDVar[OrderId](OrderId.apply)
}
