package shop.services

import shop.domain.category._
import shop.sql.codecs._
import shop.effects.GenUUID
import shop.domain.ID

import skunk._
import skunk.implicits._
import cats.syntax.all._
import cats.effect._

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[CategoryId]
}

object Categories {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Categories[F] =
    new Categories[F] {
      import CategoriesSQL._

      def findAll: F[List[Category]] = postgres.use(_.execute(selectAll))

      def create(name: CategoryName): F[CategoryId] = postgres.use { session =>
        session.prepare(insertCategory).use { cmd =>
          ID.make[F, CategoryId].flatMap { id =>
            cmd.execute(Category(id, name)).as(id)
          }
        }
      }
    }
}

private object CategoriesSQL {

  val codec: Codec[Category] =
    (categoryId ~ categoryName).imap {
      case (i ~ n) => Category(i, n)
    }(c => c.uuid ~ c.name)

  val selectAll: Query[Void, Category] =
    sql"""
      SELECT * FROM categories
    """.query(codec)

  val insertCategory: Command[Category] =
    sql"""
      INSERT INTO categories
      VALUES($codec)
    """.command
}
