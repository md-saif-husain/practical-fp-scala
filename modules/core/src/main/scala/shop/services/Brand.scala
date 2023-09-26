package shop.services

import shop.domain.brand._

trait Brands[F[_]] {
    def findAll: F[List[Brand]]
    def create(name: BrandName): F[BrandId]
}