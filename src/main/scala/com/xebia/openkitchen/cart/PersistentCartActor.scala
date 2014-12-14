package com.xebia.openkitchen
package cart

import java.util.UUID
import akka.actor.{ ActorLogging, PoisonPill, ReceiveTimeout, Props }
import akka.persistence._
import scala.concurrent.duration._
import product._
import akka.actor.actorRef2Scala
import product.ActorContextProductRepoSupport
import CartDomain._
object PersistentCartActor {

  def props = Props[PersistentCartActor]
  def name = "persistent-cart-actor"

  sealed trait Event
  case class ItemAddedEvent(itemId: String) extends Event
  case class ItemRemovedEvent(itemId: String) extends Event
  case class CartCheckedoutEvent(orderId: UUID) extends Event

}

class PersistentCartActor extends PersistentActor with ActorLogging with ActorContextProductRepoSupport {
  import PersistentCartActor._
  import productRepo._
  override def persistenceId = context.self.path.name
  val receiveTimeout: FiniteDuration = 20 seconds
  var cart = CartItems()

  val receiveCommand: Receive = {
    case AddToCartRequest(itemId) => {
      Thread.sleep(1000)
      doWithItem(itemId) { item =>
        persist(ItemAddedEvent(itemId)) { evt =>
          updateStateAndPublish(evt)
          sender ! cart.items
        }
      }
    }
    case RemoveFromCartRequest(itemId) => {
      doWithItem(itemId) { item =>
        persist(ItemRemovedEvent(itemId)) { evt =>
          updateStateAndPublish(evt)
          sender ! cart.items
        }
      }
    }
    case GetCartRequest => {
      sender ! cart.items
    }
    case OrderRequest => {
      if (cart.isEmpty) {
        sender ! OrderProcessingFailed
      } else {
        //real order processing is skipped
        val orderId: UUID = UUID.randomUUID()
        persist(CartCheckedoutEvent(orderId)) { evt =>
          updateStateAndPublish(evt)
          saveSnapshot(cart)
          sender ! OrderProcessed(orderId.toString)
        }
      }
    }
    case ReceiveTimeout => {
      log.info("received timeout")
      context.become {
        case msg @ SaveSnapshotSuccess(_) => {
          log.info("Snapshot saved. I'm going to passivate")
          publish(msg)
          self ! PoisonPill
        }
        case msg @ SaveSnapshotFailure(_, ex) => {
          log.info(s"Snapshot could not be saved due to: ${ex.getMessage}. I'm going to passivate")
          publish(msg)
          self ! PoisonPill
        }
      }
      log.info("saving snapshot")
      saveSnapshot(cart)
    }

  }

  def updateState(event: Event): Unit = {
    event match {
      case ItemAddedEvent(itemId) =>
        cart = cart.update(productMap(itemId))
      case ItemRemovedEvent(itemId) =>
        cart = cart.remove(productMap(itemId))
      case CartCheckedoutEvent(_) =>
        cart = cart.clear()
    }
  }

  val receiveRecover: Receive = {
    case e: Event =>
      log.info(s"recovery: got $e")
      updateState(e)
    case t: RecoveryCompleted =>
      log.info(s"recovery completed; setting receive timeout")
      context.setReceiveTimeout(receiveTimeout)
    case SnapshotOffer(_, shoppingCartState: CartItems) =>
      log.info(s"recovery: got snapshot: ${shoppingCartState.size} items")
      cart = shoppingCartState
  }

  private def doWithItem(itemId: String)(item: Device => Unit) = {
    val device = productRepo.productMap.get(itemId) match {
      case Some(device) => item(device)
      case None => sender ! akka.actor.Status.Failure(new IllegalArgumentException(s"Product with id $itemId not found."))
    }
  }

  private def updateStateAndPublish(event: Event) = {
    updateState(event)
    publish(event)
  }

  private def publish(msg: Object) = {
    system.eventStream.publish(msg)
  }

}