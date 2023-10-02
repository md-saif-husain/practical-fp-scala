package shop.effects

import cats.effect.IO
import scala.concurrent.duration.FiniteDuration

object TestBackground {
    val NoOp: Background[IO] = new Background[IO] {
      override def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit        
    }
}