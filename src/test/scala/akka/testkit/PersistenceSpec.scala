/**
 * Copied from Akka:
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.testkit

import akka.persistence._

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

import com.typesafe.config.ConfigFactory

import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterEach

import akka.actor.Props

trait PersistenceSpec extends BeforeAndAfterEach with Cleanup { this: AkkaSpec ⇒
  private var _name: String = _

  lazy val extension = Persistence(system)
  val counter = new AtomicInteger(0)

  /**
   * Unique name per test.
   */
  def name = _name

  /**
   * Prefix for generating a unique name per test.
   */
  def namePrefix: String = system.name

  override protected def beforeEach() {
    _name = s"${namePrefix}-${counter.incrementAndGet()}"
  }
}

object PersistenceSpec {
  def config(plugin: String, test: String, serialization: String = "on", extraConfig: Option[String] = None) =
    extraConfig.map(ConfigFactory.parseString(_)).getOrElse(ConfigFactory.empty()).withFallback(
      ConfigFactory.parseString(
        s"""
      akka.persistence.journal.leveldb.dir = "target/journal-${test}"
      akka.persistence.snapshot-store.local.dir = "target/snapshots-${test}/"
      akka.persistence.journal.leveldb.native = false
      akka.test.single-expect-default = 10s
    """))
}

trait Cleanup { this: AkkaSpec ⇒
  val storageLocations = List(
    "akka.persistence.journal.leveldb.dir",
    "akka.persistence.snapshot-store.local.dir").map(s ⇒ new File(system.settings.config.getString(s)))

  override protected def atStartup() {
    storageLocations.foreach(FileUtils.deleteDirectory)
  }

  override protected def afterTermination() {
    storageLocations.foreach(FileUtils.deleteDirectory)
  }
}

abstract class NamedPersistentActor(name: String) extends PersistentActor {
  override def persistenceId: String = name
}

class TestException(msg: String) extends Exception(msg) with NoStackTrace

case object GetState