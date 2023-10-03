package shop.http.routes

import shop.domain.item._
import shop.domain.brand._
import shop.generator._
import suite.HttpSuite
import shop.services.Items

import cats.syntax.all._
import cats.effect._
import org.scalacheck.Gen
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._

object ItemRoutesSuite extends HttpSuite {

  def dataItems(items: List[Item]) = new TestItems {
    override def findAll: IO[List[Item]]                  = IO.pure(items)
    override def findBy(brand: BrandName): IO[List[Item]] = IO.pure(items.find(_.brand.name === brand).toList)
  }

  def failingItems(items: List[Item]) = new TestItems {
    override def findAll: IO[List[Item]] = IO.raiseError(DummyError) *> IO.pure(items)
  }

  test("Get items succeed") {
    forall(Gen.listOf(itemGen)) { i =>
      val req    = GET(uri"/items")
      val routes = ItemRoutes[IO](dataItems(i)).routes
      expectHttpBodyAndStatus(routes, req)(i, Status.Ok)
    }
  }

  test("Get items by brand name succeed") {
    val gen = for {
      it <- Gen.listOf(itemGen)
      b  <- brandGen
    } yield (it, b)
    forall(gen) {
      case (it, b) =>
        val req      = GET(uri"/items".withQueryParam("brand", b.name.value))
        val routes   = ItemRoutes[IO](dataItems(it)).routes
        val expected = it.find(_.brand.name.value === b.name.value).toList
        expectHttpBodyAndStatus(routes, req)(expected, Status.Ok)
    }
  }

  test("GET items fails") {
    forall(Gen.listOf(itemGen)) { it =>
      val req    = GET(uri"/items")
      val routes = ItemRoutes[IO](failingItems(it)).routes
      expectHttpFailure(routes, req)
    }
  }

  protected class TestItems extends Items[IO] {
    override def findAll: IO[List[Item]]                    = ???
    override def findBy(brand: BrandName): IO[List[Item]]   = ???
    override def findById(itemId: ItemId): IO[Option[Item]] = ???
    override def create(item: CreateItem): IO[ItemId]       = ???
    override def update(item: UpdateItem): IO[Unit]         = ???
  }

}
