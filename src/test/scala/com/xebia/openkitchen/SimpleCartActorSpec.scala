package com.xebia.openkitchen

import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import akka.testkit.TestSupport._
import CartManagerActor._
import CartMessages._
import OrderMessages._
import ProductDomain._
import akka.actor.Props
class SimpleCartActorSpec extends Specification
  with Specs2RouteTest {

  val productRepo = ProductRepo()
  "The CartActor" should {
    "read items" in new AkkaTestkitContext() {
      val reverseActor = system.actorOf(Props(new SimpleCartActor(productRepo)), "cart-actor")
      import akka.pattern.ask

      reverseActor ! Envelope("sessionId-1", GetCartRequest)

      expectMsg(Seq())

    }
    "order" in new AkkaTestkitContext() {
      val reverseActor = system.actorOf(Props(new SimpleCartActor(productRepo)), "cart-actor")
      import akka.pattern.ask
      val product = productRepo.products.head
      reverseActor ! Envelope("sessionId-2", AddToCartRequest(product.id))

      expectMsg(Seq(ShoppingCartItem(product, 1)))

      reverseActor ! Envelope("sessionId-2", OrderRequest)
      expectMsgClass(classOf[OrderProcessed])
    }
  }
}
