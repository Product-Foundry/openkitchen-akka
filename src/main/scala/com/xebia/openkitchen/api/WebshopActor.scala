package com.xebia.openkitchen
package api

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import spray.http._
import spray.routing._
import util._
import spray.routing.directives._
import spray.http.Uri.Path
import akka.event.Logging
import com.xebia.openkitchen.cart._

object WebshopActor {
  def props = Props[WebshopActor]
  def name = "webshop-route"
}

class WebshopActor extends Actor with WebshopRoute with ActorExecutionContextSupport with ActorContextCreationSupport {

  def actorRefFactory = context
  override val cartHandler = getOrCreateChild(CartManagerActor.props(SimpleCartActor.props), "cart-manager")
  def receive = runRoute(myRoute)

}

trait WebshopRoute extends HttpService with StaticResources with Api with DirectiveExtensions {

  val myRoute =
    logRequest(showPath _) {
      shoppingCartRoutes ~ staticResources
    }

  private def showPath(req: HttpRequest): Option[LogEntry] = {
    def pathEndsWith(uri: Path, suffix: String*) = suffix.exists(uri.toString.endsWith)
    req match {
      case req @ HttpRequest(HttpMethods.POST, uri, _, entity, _) => Some(LogEntry("Method = %s, Path = %s, Data = %s" format (req.method, uri, entity), Logging.InfoLevel))
      case req @ HttpRequest(method, uri, _, _, _) if !pathEndsWith(uri.path, "css", "jpg", "js", "png") => Some(LogEntry("Method = %s, Path = %s" format (method, uri), Logging.InfoLevel))
      case _ => None
    }
  }

}