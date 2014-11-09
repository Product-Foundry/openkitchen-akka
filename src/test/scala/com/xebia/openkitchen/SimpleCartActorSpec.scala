package com.xebia.openkitchen

import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import akka.testkit.TestSupport._
import CartManagerActor._
import SimpleCartActor._
import ProductDomain._
import akka.actor.Props
class SimpleCartActorSpec extends Specification
  with Specs2RouteTest with ProductRepoSupportProvider {

  "The CartActor" should {
    "read items" in new AkkaTestkitContext() {
      val reverseActor = system.actorOf(SimpleCartActor.props)
      import akka.pattern.ask

      reverseActor ! GetCartRequest

      expectMsg(Seq())

    }
    "order" in new AkkaTestkitContext() {
      val reverseActor = system.actorOf(SimpleCartActor.props)
      import akka.pattern.ask
      val product = productRepo.products.head
      reverseActor ! AddToCartRequest(product.id)

      expectMsg(Seq(ShoppingCartItem(product, 1)))

      reverseActor ! OrderRequest
      expectMsgClass(classOf[OrderProcessed])
    }
  }
}
