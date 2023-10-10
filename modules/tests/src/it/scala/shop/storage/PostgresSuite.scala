package shop.storage

import suite.ResourceSuite
import shop.generator._

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import natchez.Trace.Implicits.noop
import org.scalacheck.Gen
import skunk._
import skunk.implicits._
import shop.services.Brands
import shop.services.Categories

object PostgresSuite extends ResourceSuite {
  type Res = Resource[IO, Session[IO]]

  val flushTables: List[Command[Void]] =
    List("items", "brands", "categories", "orders", "users").map { table =>
      sql"DELETE FROM #$table".command
    }

  def sharedResource: Resource[IO, Res] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = Some("my-password"),
        database = "store",
        max = 10
      )
      .beforeAll {
        _.use { s =>
          flushTables.traverse_(s.execute)
        }
      }

  test("Brands") { postgres =>
    forall(brandGen) { brand =>
      val b = Brands.make[IO](postgres)
      for {
        x <- b.findAll
        _ <- b.create(brand.name)
        y <- b.findAll
        z <- b.create(brand.name).attempt
      } yield expect.all(x.isEmpty, y.count(_.name === brand.name) === 1, z.isLeft)
    }
  }

  test("Category") { postgres =>
    forall(categoryGen) { category =>
      val c = Categories.make[IO](postgres)
      for {
        x <- c.findAll
        _ <- c.create(category.name)
        y <- c.findAll
        z <- c.create(category.name).attempt
      } yield expect.all(
        x.isEmpty,
        y.count(
          _.name === category.name
          ) === 1,
        z.isLeft  
      )
    }
  }
}
