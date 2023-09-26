package shop.services

import shop.domain.healthcheck._

trait HealthCheck[F[_]] {
    def status: F[AppStatus]
}