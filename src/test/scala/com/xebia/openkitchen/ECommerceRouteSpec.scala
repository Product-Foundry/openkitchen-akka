package com.xebia.openkitchen

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import akka.testkit.TestProbe
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._
import spray.routing._
import SimpleCartActor._
import akka.testkit.TestActor
import org.specs2.runner.JUnitRunner
import spray.http.HttpHeaders.Cookie
import akka.actor.ActorRef
import spray.http.HttpCookie
@RunWith(classOf[JUnitRunner])
class ECommerceRouteSpec extends Specification with Specs2RouteTest with ECommerceRoute {

  def productRepo = ProductRepo()
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