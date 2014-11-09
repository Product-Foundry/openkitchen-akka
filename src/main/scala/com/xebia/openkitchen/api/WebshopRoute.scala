package com.xebia.openkitchen
package api

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util._

import util._

import akka.actor.ActorRef
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import cart.CartManagerActor.Envelope
import product.ProductDomain._
import spray.http._
import spray.http.Uri.Path
import spray.http.Uri.apply
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing._
import spray.routing.Directive.SingleValueModifiers
import spray.routing.Directive.pimpApply
import spray.routing.directives.LogEntry
import spray.routing.directives.OnCompleteFutureMagnet.apply


// this trait defines our service behavior independently from the service actor
trait WebshopRoute extends HttpService with StaticResources with Api with DirectiveExtensions {
  

  def showPath(req: HttpRequest):Option[LogEntry] = {
    def pathEndsWith(uri: Path, suffix: String*) = suffix.exists(uri.toString.endsWith)
    req match {
      case req @ HttpRequest(HttpMethods.POST, uri, _, entity, _) => Some(LogEntry("Method = %s, Path = %s, Data = %s" format (req.method, uri, entity), Logging.InfoLevel))
      case req @ HttpRequest(method, uri, _, _, _) if !pathEndsWith(uri.path, "css", "jpg", "js", "png") => Some(LogEntry("Method = %s, Path = %s" format (method, uri), Logging.InfoLevel))
      case _ => None
    }
  }

  val myRoute =
    logRequest(showPath _) {
      shoppingCartRoutes ~ staticResources
    }
}



object Api {
  case class OrderStateResponse(state: String, orderId: Option[String] = None)

}

//REST Api
trait Api extends HttpService with JsonSerializers with DirectiveExtensions with ExecutionContextSupport with ActorAskSupport{
  import cart.SimpleCartActor._
  import cart.CartManagerActor._
  import Api._
  val cartHandler: ActorRef

  //custom directive to retrieve cookie
  val sessionId: Directive1[String] = getOrCreateSessionCookie("session-id").flatMap {
    case c: HttpCookie => provide(c.content)
  }
  val shoppingCartRoutes =
    pathPrefix("cart") {
      (post & sessionId) { sessionId =>
        entity(as[AddToCartRequest]) { addMsg =>
          handleCartRequest(Envelope(sessionId, addMsg))
        }
      } ~
        delete {
          (parameter('itemId) & sessionId) { (itemId, sessionId) =>
            handleCartRequest(Envelope(sessionId, RemoveFromCartRequest(itemId)))
          }
        } ~
        (get & sessionId) { sessionId =>
          handleCartRequest(Envelope(sessionId, GetCartRequest))
        }
    } ~ path("order") {
      (put & sessionId) { sessionId =>
        handleOrderRequest(sessionId)
      }
    }
  private def handleCartRequest[T](reqCtx: Envelope[T]) = {
    val respFuture = cartHandler.ask(reqCtx).mapTo[Seq[ShoppingCartItem]]
    onComplete(respFuture) {
      case Success(res) => complete(res)
      case Failure(e) => completeWithError(e)
    }
  }

  private def handleOrderRequest(sessionId: String) = {
    val processingStateFuture = cartHandler.ask(Envelope(sessionId, OrderRequest))
    onComplete(processingStateFuture) {
      case Success(resp) => resp match {
        case ok @ OrderProcessed(orderId) => complete(OrderStateResponse(ok.getClass.getSimpleName(), Some(orderId)))
        case nok => complete(OrderStateResponse(nok.toString))
      }
      case Failure(e) => completeWithError(e)
    }
  }
  private def completeWithError(e: Throwable) = complete(StatusCodes.InternalServerError, e.getMessage())

}

// Trait for serving static resources
// Sends 404 for 'favicon.icon' requests and serves static resources in 'bootstrap' folder.
trait StaticResources extends HttpService {

  val staticResources =
    get {
      path("") {
        redirect("/index.html", StatusCodes.PermanentRedirect)
      } ~
        path("favicon.ico") {
          complete(StatusCodes.NotFound)
        } ~
        path(Rest) { path =>
          getFromResource("root/%s" format path)
        }
    }
}