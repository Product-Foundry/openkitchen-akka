package com.xebia.openkitchen.util

import spray.routing._
import spray.http.HttpCookie
import java.util.UUID
import spray.routing.directives.LoggingMagnet
import spray.util.LoggingContext
import spray.routing.directives.LogEntry

trait DirectiveExtensions extends SessionDirectives with LoggingExtension

trait SessionDirectives extends Directive1Extension {
  def getOrCreateSessionCookie(name: String): Directive1[HttpCookie] = optionalCookie(name).flatMap {
    case Some(c) => provide(c)
    case None => {
      val sessionCookie = HttpCookie(name, UUID.randomUUID().toString())
      liftToDirective1(setCookie(sessionCookie), sessionCookie)
    }
  }
  //custom directive to retrieve cookie
  val sessionId: Directive1[String] = getOrCreateSessionCookie("session-id").flatMap {
    case c: HttpCookie => provide(c.content)
  }
}
trait LoggingExtension {
  implicit def forMessageFromFullShow[T](show: T ⇒ Option[LogEntry])(implicit log: LoggingContext): LoggingMagnet[T ⇒ Unit] = // # message-magnets
    LoggingMagnet(show(_).map(_.logTo(log)))

}

trait Directive1Extension extends Directives {
  import shapeless._
  def liftToDirective1[T](f: Directive0, value: T): Directive1[T] = new Directive1[T] {
    def happly(inner: T :: HNil ⇒ Route) = f(inner(value :: HNil))
  }
}
