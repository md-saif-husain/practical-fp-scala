package shop.http.routes


import shop.domain.ID
import shop.domain.brand._
import shop.services.Brands
import shop.generator._ 

import cats.effect._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen
import suite.HttpSuite

object BrandRoutesSuite extends HttpSuite {
    def databrands(brands: List[Brand]) = new TestBrands {
        override def findAll: IO[List[Brand]] = IO.pure(brands)
    }

    test("Get brands succeed"){
        forall(Gen.listOf(brandGen)) { b =>
            val req = GET(uri"/brands")
            val routes = BrandRoutes[IO](databrands(b)).routes
            expectHttpBodyAndStatus(routes, req)(b, Status.Ok)

        }
    }

    protected class TestBrands extends Brands[IO] {
        def create(name: BrandName): IO[BrandId] = ???
        def findAll: IO[List[Brand]] = ???            
    }
}
