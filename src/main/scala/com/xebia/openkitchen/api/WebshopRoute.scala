package com.xebia.openkitchen
package api

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util._

import util._

import akka.actor.ActorRef

import akka.pattern.ask
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing._
import spray.routing.Directive._
import spray.routing.directives.OnCompleteFutureMagnet.apply
import cart.CartManagerActor.Envelope
import cart.CartDomain._ 
import product._

object Api {
  case class OrderStateResponse(state: String, orderId: Option[String] = None)
}

trait Api extends HttpService with JsonSerializers with DirectiveExtensions with ExecutionContextSupport with ActorAskSupport {
  import cart.SimpleCartActor._
  import cart.CartManagerActor._
  import Api._
  val cartHandler: ActorRef

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
    val respFuture = cartHandler.ask(reqCtx).mapTo[Seq[CartItem]]
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