package com.xebia.openkitchen
package api

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import akka.testkit.TestProbe
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._
import spray.routing._
import cart.SimpleCartActor._
import akka.testkit.TestActor
import spray.http.HttpHeaders.Cookie
import akka.actor.ActorRef
import spray.http.HttpCookie
import akka.actor.actorRef2Scala
import com.xebia.openkitchen.product.ProductStore
import org.specs2.runner.JUnitRunner
import util._
@RunWith(classOf[JUnitRunner])
class WebshopRouteSpec extends Specification with Specs2RouteTest with WebshopRoute with ActorSystemContextSupport{

  def productRepo = ProductStore()
  def actorRefFactory = system
  override val cartHandler = {
    val probe = TestProbe()
    probe.setAutoPilot {
      new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any) = msg match {
          case _ =>
            sender ! Seq(ShoppingCartItem(productRepo.products.head, 1))
            TestActor.KeepRunning
        }
      }
    }
    probe.ref
  }
  "ECommerce route" should {

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> myRoute ~> check {
        handled must beFalse
      }
    }

    "GET requests to root must be redirected" in {
      Get("/") ~> myRoute ~> check {
        handled must beTrue
      }
    }
    "GET requests to cart must return shopping cart" in {
      Get("/cart") ~> Cookie(HttpCookie("session-id", content = "id")) ~> myRoute ~> check {
        status === OK
        responseAs[Seq[ShoppingCartItem]] === Seq(ShoppingCartItem(productRepo.products.head, 1))
      }
    }
  }

}