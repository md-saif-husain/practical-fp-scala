package shop.storage

import shop.domain.brand._
import shop.domain.category._
import shop.domain.item._
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
import shop.domain.brand
import shop.services.Items
import shop.services.Users

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

  test("Items") { postgres =>
    forall(itemGen) { item =>
      def newItem(
          bid: Option[BrandId],
          cid: Option[CategoryId]
      ) = CreateItem(
        name = item.name,
        description = item.description,
        price = item.price,
        brandId = bid.getOrElse(item.brand.uuid),
        categoryId = cid.getOrElse(item.category.uuid)
      )

      val b = Brands.make[IO](postgres)
      val c = Categories.make[IO](postgres)
      val i = Items.make[IO](postgres)

      for {
        x <- i.findAll
        _ <- b.create(item.brand.name)
        d <- b.findAll.map(_.headOption.map(_.uuid))
        _ <- c.create(item.category.name)
        e <- c.findAll.map(_.headOption.map(_.uuid))
        _ <- i.create(newItem(d, e))
        y <- i.findAll
      } yield expect.all(
        x.isEmpty,
        y.count(_.name === item.name) === 1
      )
    }
  }

  test("Users") { postgres =>
    val gen = for {
      u <- userNameGen
      p <- encryptedPasswordGen
    } yield u -> p

    forall(gen) {
      case (username, password) =>
        val u = Users.make[IO](postgres)
        for {
          d <- u.create(username, password)
          x <- u.find(username)
          z <- u.create(username, password).attempt
        } yield expect.all(
          x.count(_.id === d) === 1,
          z.isLeft
        )
    }
  }
}
