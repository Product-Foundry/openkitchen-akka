package com.xebia.openkitchen
import akka.testkit.TestSupport._
import scala.concurrent.duration._
import akka.testkit.PersistenceSpec
import akka.testkit.ImplicitSender
import akka.testkit.AkkaSpec
import CartManagerActor._
import CartMessages._
import OrderMessages._
import ProductDomain._
trait DeactivatedTimeConversions extends org.specs2.time.TimeConversions {
  override def intToRichLong(v: Int) = super.intToRichLong(v)
}

class PersistentCartActorSpec extends AkkaSpec(PersistenceSpec.config("leveldb", "ShoppingCartActorSpec")) with PersistenceSpec with ImplicitSender {
  val productRepo = ProductRepo()
  "The CartActor" should { 
    val product = productRepo.products.head

    "return carts contents" in {
      val cart = system.actorOf(PersistentCartActor.props(productRepo))
      cart ! GetCartRequest
      expectMsg(Seq())

      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(ShoppingCartItem(product, 1)))

      cart ! GetCartRequest
      expectMsg(Seq(ShoppingCartItem(product, 1)))
    }
    "add item to cart" in {
      val cart = system.actorOf(PersistentCartActor.props(productRepo))
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(ShoppingCartItem(product, 1)))
    }
    "update cart when existing item is added" in {
      val cart = system.actorOf(PersistentCartActor.props(productRepo))
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(ShoppingCartItem(product, 1)))

      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(ShoppingCartItem(product, 2)))
    }
    "remove item from cart" in {
      val cart = system.actorOf(PersistentCartActor.props(productRepo))
      cart ! AddToCartRequest(product.id)
      expectMsg(Seq(ShoppingCartItem(product, 1)))

      cart ! RemoveFromCartRequest(product.id)
      expectMsg(Seq())
    }
  }
}

