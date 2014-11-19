package com.xebia.openkitchen
package cart

import scala.concurrent.duration._
import akka.testkit.PersistenceSpec
import akka.testkit.ImplicitSender
import akka.testkit.AkkaSpec
import akka.testkit.TestProbe
import akka.actor.Terminated
import scala.util.Random._
import CartManagerActor._
import SimpleCartActor._
import product._
import cart.CartDomain._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class PersistentCartActorSpec extends AkkaSpec(PersistenceSpec.config("leveldb", "ShoppingCartActorSpec")) with PersistenceSpec with ImplicitSender with ProductStoreSupportProvider {
  import PersistentCartActor._
  "The CartActor" should {
    val product = productRepo.products.head
    val eventStreamProbe = TestProbe()
    system.eventStream.subscribe(eventStreamProbe.ref, classOf[Event])

    "return carts contents" in {
      val cart = createActorUnderTest(random())
      cart ! GetCartRequest
      expectMsg(Seq())

      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 1)))

      cart ! GetCartRequest
      expectMsg(Seq(CartItem(product, 1)))
    }
    "add item to cart" in {
      val cart = createActorUnderTest(random())
      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 1)))
    }
    "update cart when existing item is added" in {
      val cart = createActorUnderTest(random())
      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 1)))

      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 2)))
    }
    "remove item from cart" in {
      val cart = createActorUnderTest(random())
      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 1)))

      cart ! RemoveFromCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemRemovedEvent(product.id))
      expectMsg(Seq())
    }
    "correctly recover the basket state from the journal" in {
      val randomId = random()
      val cart = createActorUnderTest(randomId)
      cart ! AddToCartRequest(product.id)
      eventStreamProbe.expectMsg(ItemAddedEvent(product.id))
      expectMsg(Seq(CartItem(product, 1)))

      watch(cart)
      system.stop(cart)

      expectMsgPF() {
        case Terminated(cart) ⇒
          val recoveredCart = createActorUnderTest(randomId)
          recoveredCart ! GetCartRequest
          expectMsg(Seq(CartItem(product, 1)))
          system.stop(recoveredCart)
      }
    }
  }

  def random() = nextInt
  
  def createActorUnderTest(id: Any) = {
    system.actorOf(PersistentCartActor.props, id.toString)
  }
}

