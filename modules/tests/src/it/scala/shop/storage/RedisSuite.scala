package shop.storage

import scala.concurrent.duration._

import shop.auth._
import shop.config.types._
import shop.domain.ID
import shop.domain.auth._
import shop.domain.brand._
import shop.domain.cart._
import shop.domain.category._
import shop.domain.item._
import shop.generator._
import shop.http.auth.users._
import shop.services._

import cats.effect._
import cats.effect.kernel.Ref
import cats.implicits._
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.log4cats._
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import org.typelevel.log4cats.noop.NoOpLogger
import pdi.jwt._
import suite.ResourceSuite

object RedisSuite extends ResourceSuite {
  implicit val logger = NoOpLogger[IO]

  type Res = RedisCommands[IO, String, String]

  def sharedResource: Resource[IO, Res] =
    Redis[IO]
      .utf8("redis: //localhost")
      .beforeAll(_.flushAll)

  val Exp         = ShoppingCartExpiration(30.seconds)
  val tokenConfig = JwtAccessTokenKeyConfig("bar")
  val tokenExp    = TokenExpiration(30.seconds)
  val jwtClaim    = JwtClaim("test")
  val userJwtAuth = UserJwtAuth(JwtAuth.hmac("bar", JwtAlgorithm.HS256))

  

  protected class TestItems(ref: Ref[IO, Map[ItemId, Item]]) extends Items[IO] {

    def findAll: IO[List[Item]] = ref.get.map(_.values.toList)

    def findBy(brand: BrandName): IO[List[Item]] =
      ref.get.map {
        _.values.filter(_.brand.name === brand).toList
      }

    def findById(itemId: ItemId): IO[Option[Item]] = ref.get.map(_.get(itemId))

    def create(item: CreateItem): IO[ItemId] =
      ID.make[IO, ItemId].flatTap { id =>
        val brand    = Brand(item.brandId, BrandName("foo"))
        val category = Category(item.categoryId, CategoryName("foo"))
        val newItem = Item(
          id,
          item.name,
          item.description,
          item.price,
          brand,
          category
        )

        ref.update(_.updated(id, newItem))
      }

    def update(item: UpdateItem): IO[Unit] =
      ref.update(
        x =>
          x.get(item.id)
            .fold(x)(i => x.updated(item.id, i.copy(price = item.price)))
      )
  }
}
