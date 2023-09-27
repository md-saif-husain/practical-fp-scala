package shop.ext.derevo

import derevo.Derivation
import derevo.NewTypeDerivation
import scala.annotation.implicitNotFound

trait Derive[F[_]] extends Derivation[F] with NewTypeDerivation[F] {

  def instances(implicit ev: OnlyNewTypes): Nothing = ev.absurd

  @implicitNotFound("only new types can be derived")
  abstract final class OnlyNewTypes {
    def absurd: Nothing = ???

  }

}
