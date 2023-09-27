package shop.retries

import derevo.cats.show
import derevo.derive

// Algebraic Data type for retriable actions
@derive(show)
sealed trait Retriable

object Retriable {
  case object Orders   extends Retriable
  case object Payments extends Retriable
}
