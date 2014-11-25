package com.xebia.openkitchen
package cart

import java.util.UUID

import product.Device
import CartManagerActor.Envelope
import akka.actor._
import akka.actor.ActorLogging
import product.ActorContextProductRepoSupport
import CartDomain._

object SimpleCartActor {
  def props = Props[SimpleCartActor]
  def name = "simple-cart-actor"
}

class SimpleCartActor extends Actor with ActorLogging with ActorContextProductRepoSupport {
  import SimpleCartActor._
  var cart = CartItems()

  override def receive: Receive = {
    case AddToCartRequest(itemId) => ???
    case RemoveFromCartRequest(itemId) => ???
    case GetCartRequest => ???
    case OrderRequest => ???
  }

  private def doWithItem(itemId: String)(item: Device => Unit) = {
    val device = productRepo.productMap.get(itemId) match {
      case Some(device) => item(device)
      case None => sender ! akka.actor.Status.Failure(new IllegalArgumentException(s"Product with id $itemId not found."))
    }
  }

  private def processOrder(): OrderState = {
    if (!cart.items.isEmpty) {
      log.info(s"place order for items: $cart")
      cart = cart.clear()
      //real order processing is skipped
      OrderProcessed(UUID.randomUUID().toString)
    } else {
      OrderProcessingFailed
    }
  }

}