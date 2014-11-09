package com.xebia.openkitchen


import akka.actor._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import akka.testkit.TestSupport._
import akka.testkit._
import cart.CartManagerActor._
import com.xebia.openkitchen.cart.CartManagerActor
import org.specs2.runner.JUnitRunner
import akka.contrib.pattern.ShardRegion.Passivate
@RunWith(classOf[JUnitRunner])
class CartMangerActorSpec extends Specification {

  "Cart manager" should {
    "create new cart actors for new sessions" in new AkkaTestkitContext() {

      //      val cartActorStubProps = Props(new Actor {
      //        override def receive: Receive = {
      //          case m: String => sender ! s"Echoing: $m"
      //        }
      //      }) 

      class Wrapper(target: ActorRef) extends Actor {
        def receive = {
          case x => target forward x
        }
      }

      val probeActorRef = {
        val probe = TestProbe()
        probe.setAutoPilot {
          new TestActor.AutoPilot {
            def run(sender: ActorRef, msg: Any) = msg match {
              case _ =>
                sender ! s"Echoing: $msg"
                TestActor.KeepRunning
            }
          }
        }
        probe.ref
      }

      val probe = new TestProbe(system) 
      val cartActorStubProps = Props(new Wrapper(probe.ref))

      val cartManager = system.actorOf(Props(new CartManagerActor(cartActorStubProps)))

      cartManager ! Envelope("aaaaa", "Bla")

      probe.expectMsg("Bla")
      //expectMsg("Echoing: Bla")

      system.actorSelection(cartManager.path.child("*")) ! Identify()

      expectMsgClass(classOf[ActorIdentity]).ref.map(_.path.name) must be equalTo Some("aaaaa")

      cartManager ! Envelope("aaaaa", "Foo")
      cartManager ! Envelope("bbbbb", "Bar")

       probe.expectMsg("Foo")
        probe.expectMsg("Bar")
//      expectMsg("Echoing: Foo")
//      expectMsg("Echoing: Bar")

      system.actorSelection(cartManager.path.child("*")) ! Identify()

      val ids = Set(expectMsgClass(classOf[ActorIdentity]), expectMsgClass(classOf[ActorIdentity])).map(_.ref.map(_.path.name))
      ids must be equalTo Set(Some("aaaaa"), Some("bbbbb"))

    }

    "reply with passivation content back to sender" in new AkkaTestkitContext() {

      val cartActorStubProps = Props(new Actor {
        override def receive: Receive = {
          case m: String => sender ! s"Echoing: $m"
        }
      })

      val cartManager = system.actorOf(Props(new CartManagerActor(cartActorStubProps)))

      cartManager ! Passivate("Content")
      expectMsg("Content")

    }
  }

}