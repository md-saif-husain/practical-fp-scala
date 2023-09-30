package shop.http.routes.secured

import shop.services.ShoppingCart
import shop.http.auth.users._
import shop.domain.cart._
import shop.http.vars._

import cats.Monad
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._




final case class CartRoutes[F[_]: JsonDecoder: Monad](
    shoppingCart: ShoppingCart[F]
) extends Http4sDsl[F] {
    private [routes] val prefixPath = "/carts"

    private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
        // Get Shopping Cart
        case GET -> Root as user  => 
            Ok(shoppingCart.get(user.value.id))

        // Add items to Cart
        case ar @ POST -> Root as user =>
            ar.req.asJsonDecode[Cart].flatMap {
                _.items.map {
                    case (id, quantity) => 
                        shoppingCart.add(user.value.id, id, quantity)
                }
                .toList
                .sequence *> Created()
            }

        // Delete item from Cart
        case DELETE -> Root / ItemIdVar(itemId) as user => 
            shoppingCart.removeItem(user.value.id, itemId) *> NoContent()

        // Modify item in Cart
        case ar @ PUT -> Root as user => 
            ar.req.asJsonDecode[Cart].flatMap { cart => 
                shoppingCart.update(user.value.id, cart) *> NoContent()                
            }             

    }

    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
        prefixPath -> authMiddleware(httpRoutes)
    )


}