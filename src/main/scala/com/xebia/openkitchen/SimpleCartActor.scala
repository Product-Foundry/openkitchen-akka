package com.xebia.openkitchen

import java.util.UUID

import ProductDomain.Device
import CartManagerActor.Envelope
import akka.actor._
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
  
  def props = Props(classOf[SimpleCartActor], "simple-cart-actor")
  
  case class CartItems(items: Seq[ShoppingCartItem] = Seq()) {
    def update(item: Device) = {
      val updatedItem = items.find(_.item.id == item.id)
        .map(item => item.copy(count = (item.count + 1)))
        .getOrElse(ShoppingCartItem(item))
      copy(items = (items.filterNot(_.item.id == item.id) :+ updatedItem))
    }
    def remove(item: Device) = {
      copy(items = items.filterNot(_.item.id == item.id))
    }

    def clear() = copy(items = Seq())
    def size = items.size
    def isEmpty = items.isEmpty
    override def toString = s"${CartItems.getClass().getSimpleName()} ${items.map(_.item.name).mkString}"
  }
 
}


class SimpleCartActor extends Actor with ActorLogging with ActorContextProductRepoSupport {

  import SimpleCartActor._
  log.info(s"Creating a new ShoppingCartActor")
  var cart = CartItems()

  override def receive: Receive = {
    case AddToCartRequest(itemId) => {
      doWithItem(itemId) { item =>
        log.info(s"update cart with item: ${item.name}")
        cart = cart.update(item)
        sender ! cart.items
      }
    }
    case RemoveFromCartRequest(itemId) => {
      doWithItem(itemId) { item =>
        log.info(s"remove item: ${item.name} from cart")
        cart = cart.remove(item)
        sender ! cart.items
      }
    }
    case GetCartRequest => {
      log.info(s"get items from cart: ${cart}")
      sender ! cart.items
    }
    case OrderRequest => {
      val orderState = processOrder()
      sender ! orderState
    }
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
      //send items to order actor
      OrderProcessed(UUID.randomUUID().toString)
    } else {
      OrderProcessingFailed
    }

  }

}