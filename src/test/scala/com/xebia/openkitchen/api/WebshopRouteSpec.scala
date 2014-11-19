package com.xebia.openkitchen
package api

import org.junit.runner.RunWith
import akka.testkit.TestProbe
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import akka.testkit.TestActor
import spray.http.HttpHeaders.Cookie
import akka.actor.ActorRef
import spray.http.HttpCookie
import akka.actor.actorRef2Scala
import product.ProductStore
import util._
import cart.CartDomain._
import spray.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class WebshopRouteSpec extends WordSpecLike with Matchers with ScalatestRouteTest with WebshopRoute with ActorSystemContextSupport with JsonSerializers {

  def productRepo = ProductStore()
  def actorRefFactory = system
  override val cartHandler = {
    val probe = TestProbe()
    probe.setAutoPilot {
      new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any) = msg match {
          case _ =>
            sender ! Seq(CartItem(productRepo.products.head, 1))
            TestActor.KeepRunning
        }
      }
    }
    probe.ref
  }
  "WebShop route" should {

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> myRoute ~> check {
        handled should be(false)
      }
    }

    "GET requests to root must be redirected" in {
      Get("/") ~> myRoute ~> check {
        handled should be(true)
      }
    }
    "GET requests to cart must return shopping cart" in {
      Get("/cart") ~> Cookie(HttpCookie("session-id", content = "id")) ~> myRoute ~> check {
        status should be(OK)
        responseAs[Seq[CartItem]] should be(Seq(CartItem(productRepo.products.head, 1)))
      }
    }
  }

}