package com.xebia.openkitchen
package api

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import spray.http._
import spray.routing._
import util._

object WebshopActor {
  def props(cartHandlerProps: Props) = Props(new WebshopActor(cartHandlerProps))
  def name = "webshop-route"
}
// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class WebshopActor(val cartHandlerProps: Props) extends Actor with WebshopRoute with ActorExecutionContextSupport with ActorContextCreationSupport {
  def actorRefFactory = context
  override val cartHandler = getOrCreateChild(cartHandlerProps, "cart-manager")
  def receive = runRoute(myRoute)
}

