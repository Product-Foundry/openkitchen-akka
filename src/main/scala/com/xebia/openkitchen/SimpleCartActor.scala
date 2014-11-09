package com.xebia.openkitchen

import java.util.UUID

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
object SimpleCartActor {
  
  case class AddToCartRequest(itemId: String)
  case class RemoveFromCartRequest(itemId: String)
  case object GetCartRequest
  case class ShoppingCartItem(item: Device, count: Int = 1)
  
  case object OrderRequest
    sealed trait OrderState
  case class OrderProcessed(orderId: String) extends OrderState
  case object OrderProcessingFailed extends OrderState

 
}


class SimpleCartActor(productRepo: ProductRepo) extends Actor with ActorLogging {

  import SimpleCartActor._
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