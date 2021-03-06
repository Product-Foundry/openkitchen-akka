package com.xebia.openkitchen

import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import spray.can.Http.Bind
import api.WebshopActor
import cart._
trait WebApp extends App {

  implicit val system = ActorSystem("webshop")

  // create and start our service actor
  val service = system.actorOf(WebshopActor.props, WebshopActor.name)

  // To run project on Heroku, get PORT from environment
  val httpHost = "0.0.0.0"
  val httpPort = Option(System.getenv("PORT")).getOrElse("8080").toInt

  // create a new HttpServer using our handler tell it where to bind to
  IO(Http) ! Bind(listener= service, interface = httpHost, port=httpPort)

}

object Boot extends App with WebApp {

}