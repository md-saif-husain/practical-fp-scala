package shop.effects

import cats.effect.IO
import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Ref

object TestBackground {
  val NoOp: Background[IO] = new Background[IO] {
    override def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit
  }

  def counter(ref: Ref[IO, (Int, FiniteDuration)]): Background[IO] = new Background[IO] {
    def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = ref.update {
      case (n, t) => (n + 1, t + duration)
    }
  }
}
