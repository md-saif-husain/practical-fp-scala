package shop.http.routes

import shop.domain.category._
import shop.services.Categories
import shop.generator._
import suite.HttpSuite

import cats.effect._
import org.scalacheck.Gen
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._

object CategoryRoutesSuite extends HttpSuite {
  def dataCategories(categories: List[Category]) = new TestCategories {
    override def findAll: IO[List[Category]] = IO.pure(categories)
  }

  def failCategories(categories: List[Category]) = new TestCategories {
    override def findAll: IO[List[Category]] = IO.raiseError(DummyError) *> IO.pure(categories)
  }

  test("Get categories succeed") {
    forall(Gen.listOf(categoryGen)) { ct =>
      val req    = GET(uri"/categories")
      val routes = CategoryRoutes[IO](dataCategories(ct)).routes
      expectHttpBodyAndStatus(routes, req)(ct, Status.Ok)
    }
  }

  test("Get categories fails") {
    forall(Gen.listOf(categoryGen)) { ct =>
      val req    = GET(uri"/categories")
      val routes = CategoryRoutes[IO](failCategories(ct)).routes
      expectHttpFailure(routes, req)
    }
  }

  protected class TestCategories extends Categories[IO] {
    override def findAll: IO[List[Category]]                = ???
    override def create(name: CategoryName): IO[CategoryId] = ???
  }

}
