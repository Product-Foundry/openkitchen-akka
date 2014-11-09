package com.xebia.openkitchen
package cart

import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import akka.testkit.TestSupport._
import CartManagerActor._
import SimpleCartActor._
import product.ProductDomain._
import product._
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
