package com.xebia.openkitchen
package cart
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.contrib.pattern.ShardRegion.Passivate
import util._
import akka.actor.actorRef2Scala

object CartManagerActor {
  def props(cartProps: Props) = Props(new CartManagerActor(cartProps))
  def name = "cart-manager-actor"

  case class Envelope[T](sessionId: String, t: T)
}

class CartManagerActor(shoppingCartProps: Props) extends Actor with ActorContextCreationSupport with ActorLogging {
  import CartManagerActor._
  
  override def receive: Receive = {
    case Envelope(sessionId, payload) =>
      getOrCreateChild(shoppingCartProps, sessionId) forward payload
  }

}
