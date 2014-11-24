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
  case object SaveSnapshotAndDie

}

class PersistentCartActor extends PersistentActor with ActorLogging with ActorContextProductRepoSupport {
  import PersistentCartActor._
  import productRepo._
  override def persistenceId = ???
  var cart = CartItems()

  val receiveCommand: Receive = {
    case AddToCartRequest(itemId) => ???
    case RemoveFromCartRequest(itemId) => ???
    case GetCartRequest => ???
    case OrderRequest => {
      if (cart.isEmpty) {
        sender ! OrderProcessingFailed
      } else {
        ???
      }
    }
  }

  def updateState(event: Event): Unit = ???

  def receiveRecover: Receive = ???

  
  
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

  private def publish(event: Event) = {
    system.eventStream.publish(event)
  }

}