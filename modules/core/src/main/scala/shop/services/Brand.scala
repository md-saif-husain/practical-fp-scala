package shop.services

import shop.domain.brand._
import shop.sql.codecs._

import cats.syntax.all._
import skunk._
import skunk.implicits._
import shop.effects.GenUUID
import cats.effect._
import shop.domain.ID

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}

object Brands {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Brands[F] =
    new Brands[F] {
      import BrandSQL._

      def findAll: F[List[Brand]] = postgres.use(_.execute(selectAll))

      def create(name: BrandName): F[BrandId] = postgres.use { session =>
        session.prepare(insertBrand).use { cmd =>
          ID.make[F, BrandId].flatMap { id =>
            cmd.execute(Brand(id, name)).as(id)
          }
        }
      }
    }
}

private object BrandSQL {

  val codec: Codec[Brand] =
    (brandId ~ brandName).imap {
      case (i ~ n) => Brand(i, n)
    }(b => b.uuid ~ b.name)

  val selectAll: Query[Void, Brand] =
    sql"""
       SELECT * FROM brands
    """.query(codec)

  val insertBrand: Command[Brand] =
    sql"""
      INSERT INTO brands
      VALUES($codec)
    """.command

}
