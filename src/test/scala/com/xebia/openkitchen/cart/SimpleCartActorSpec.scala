package com.xebia.openkitchen
package cart

import spray.testkit.Specs2RouteTest
import akka.testkit.TestSupport._
import CartManagerActor._
import product._
import cart.CartDomain._
import akka.testkit.AkkaSpec
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class SimpleCartActorSpec extends AkkaSpec with ProductStoreSupportProvider with ImplicitSender  {
  val product = productRepo.products.head
  "The CartActor" should {
    "return carts contents" in {
      val cart = createActorUnderTest()
      cart ! GetCartRequest
      expectMsg(Seq())

      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 1)))

      cart ! GetCartRequest
      expectMsg(Seq(CartItem(product, 1)))
    }
    "add item to cart" in {
      val cart = createActorUnderTest()
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 1)))
    }
    "update cart when existing item is added" in {
      val cart = createActorUnderTest()
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 1)))

      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 2)))
    }
    "remove item from cart" in {
      val cart = createActorUnderTest()
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 1)))

      cart ! RemoveFromCartRequest(product.id)
      expectMsg(Seq())
    }
    "place order" in {
      val cart = createActorUnderTest()
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(CartItem(product, 1)))

      cart ! OrderRequest
      expectMsgClass(classOf[OrderProcessed])
    }
  }

  private def createActorUnderTest() = system.actorOf(SimpleCartActor.props)

}
