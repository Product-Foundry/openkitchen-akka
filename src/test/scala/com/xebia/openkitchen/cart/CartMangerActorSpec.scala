package com.xebia.openkitchen
package cart
import akka.actor._
import akka.testkit._
import cart.CartManagerActor._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class CartMangerActorSpec extends AkkaSpec with ImplicitSender {

  "Lab 1: Cart manager" should {
    "create new cart actors for new sessions" in {

      val cartManager = system.actorOf(Props(new CartManagerActor(targetActorProps)))

      cartManager ! Envelope("aaaaa", "Bla")
      expectMsg("Echoing: Bla")

      system.actorSelection(cartManager.path.child("*")) ! Identify("id")
      expectMsgClass(classOf[ActorIdentity]).ref.map(_.path.name) should be(Some("aaaaa"))

      cartManager ! Envelope("aaaaa", "Foo")
      cartManager ! Envelope("bbbbb", "Bar")

      expectMsg("Echoing: Foo")
      expectMsg("Echoing: Bar")

      system.actorSelection(cartManager.path.child("*")) ! Identify("id")

      val ids = Set(expectMsgClass(classOf[ActorIdentity]), expectMsgClass(classOf[ActorIdentity])).map(_.ref.map(_.path.name))
      ids should be(Set(Some("aaaaa"), Some("bbbbb")))

    }

  }

  private def targetActorProps = Props(new Actor {
    override def receive: Receive = {
      case m: String => sender ! s"Echoing: $m"
    }
  })

}