package com.xebia.openkitchen

import java.util.UUID

import CartMessages.AddToCartRequest
import CartMessages.GetCartRequest
import CartMessages.OrderRequest
import CartMessages.RemoveFromCartRequest
import OrderMessages.OrderProcessed
import OrderMessages.OrderProcessingFailed
import OrderMessages.OrderState
import ProductDomain.Device
import CartManagerActor.Envelope
import SessionRepo.checkoutCart
import SessionRepo.getCartItems
import SessionRepo.removeFromCart
import SessionRepo.upsertCart
import akka.actor.Actor
import akka.actor.ActorLogging
/**
 * TODO: transform this actor into a stateful actor making use of Event Sourcing to persist the
 * cart state
 */
class SimpleCartActor(productRepo: ProductRepo) extends Actor with ActorLogging {

  log.info(s"Creating a new ShoppingCartActor")
  import SessionRepo._
  override def receive: Receive = {
    case Envelope(sessionId, AddToCartRequest(itemId)) => {
      doWithItem(itemId) { item =>
        log.info(s"$sessionId: update cart with item: ${item.name}")
        val items = upsertCart(sessionId, item)
        sender ! items
      }
    }
    case Envelope(sessionId, RemoveFromCartRequest(itemId)) => {
      doWithItem(itemId) { item =>
        log.info(s"$sessionId: remove item: ${item.name} from cart")
        val items = removeFromCart(sessionId, item)
        sender ! items
      }
    }
    case Envelope(sessionId, GetCartRequest) => {
      val items = getCartItems(sessionId)
      log.info(s"$sessionId: get items from cart: ${items.map(_.item.name).mkString}")
      sender ! items
    }
    case Envelope(sessionId, OrderRequest) => {
      val orderState = processOrder(sessionId)
      sender ! orderState
    }
  }

  private def doWithItem(itemId: String)(item: Device => Unit) = {
    val device = productRepo.productMap.get(itemId) match {
      case Some(device) => item(device)
      case None => sender ! akka.actor.Status.Failure(new IllegalArgumentException(s"Product with id $itemId not found."))
    }
  }

  private def processOrder(sessionId: String): OrderState = {
    val items = checkoutCart(sessionId)
    if (!items.isEmpty) {
      log.info(s"$sessionId: place order for items: ${items.map(_.item.name).mkString}")
      //send items to order actor
      OrderProcessed(UUID.randomUUID().toString)
    } else {
      OrderProcessingFailed
    }

  }

}