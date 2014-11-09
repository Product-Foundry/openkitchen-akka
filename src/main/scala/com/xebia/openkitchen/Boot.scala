package com.xebia.openkitchen

import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import spray.can.Http.Bind
trait WebApp extends App {

  implicit val system = ActorSystem("webshop")

  val cartHandlerProps = CartManagerActor.props(SimpleCartActor.props)
  // create and start our service actor
  val service = system.actorOf(ECommerceActor.props(cartHandlerProps), ECommerceActor.name)

  // To run project on Heroku, get PORT from environment
  val httpHost = "0.0.0.0"
  val httpPort = Option(System.getenv("PORT")).getOrElse("8080").toInt

  // create a new HttpServer using our handler tell it where to bind to
  IO(Http) ! Bind(listener= service, interface = httpHost, port=httpPort)

}

object Boot extends App with WebApp {

}