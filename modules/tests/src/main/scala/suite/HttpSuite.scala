package suite

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import weaver._
import weaver.scalacheck.Checkers

trait HttpSuite extends SimpleIOSuite with Checkers {
  def expectHttpBodyAndStatus[A: Encoder](
      routes: HttpRoutes[IO],
      req: Request[IO]
  )(
      expectedBody: A,
      expectedStatus: Status
  ): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.asJson.map { json =>
          expect.same(resp.status, expectedStatus) |+|
            expect.same(
              json.dropNullValues,
              expectedBody.asJson.dropNullValues
            )
        }
      case None => IO.pure(failure("route not found"))
    }
}
