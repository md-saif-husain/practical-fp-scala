package shop.domain

import cats.kernel.laws.discipline.MonoidTests
import weaver.FunSuite
import weaver.discipline.Discipline
import org.scalacheck.Arbitrary
import squants.market.Money
import shop.generator

object OrphanSuite extends FunSuite with Discipline {
    implicit val arbMoney: Arbitrary[Money] = Arbitrary(generator.moneyGen)

    checkAll("Monoid[Money]", MonoidTests[Money].monoid)

}