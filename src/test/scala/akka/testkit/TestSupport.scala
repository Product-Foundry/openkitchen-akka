package akka.testkit

import akka.actor.ActorSystem
import org.specs2.mutable.After
import com.typesafe.config.ConfigFactory
import java.io.File
import org.specs2.mutable.Before
import org.apache.commons.io.FileUtils

object TestSupport {

  /** Simple specs2 bridge for Akka TestKit. */
  abstract class AkkaTestkitContext(actorSystem: ActorSystem) extends TestKit(actorSystem) with ImplicitSender with After {
    private[testkit] var owner = false

    def this() = {
      this(ActorSystem())
      owner = true
    }

    def after {
      if (owner) system.shutdown()
    }
  }

  /** Simple specs2 bridge for Akka TestKit. */
  abstract class AkkaPersistentTestkitContext(actorSystem: ActorSystem) extends AkkaTestkitContext(actorSystem) with Before {
    def this() = {
      this(ActorSystem("TestActorSystem", ConfigFactory.parseString(
        """
        |akka.loglevel = "DEBUG"
        |   akka.persistence {
		|	journal.leveldb.dir = "target/example/journal"
		|	snapshot-store.local.dir = "target/example/snapshots"
		|
		|	# DO NOT USE THIS IN PRODUCTION !!!
		|	# See also https://github.com/typesafehub/activator/issues/287
		|	journal.leveldb.native = false
        | }
        |akka.actor.debug {
        |   receive = on
        |   autoreceive = on
        |   lifecycle = on
        |}
      """.stripMargin)))
      owner = true
    }

    protected def cleanup() = {
      deleteLocalJournalByConfigKey("akka.persistence.journal.leveldb.dir")
      deleteLocalJournalByConfigKey("akka.persistence.snapshot-store.local.dir")
      def deleteLocalJournalByConfigKey(configKey: String) = {
        val dir = system.settings.config.getString(configKey)
        import util.control.Exception._
        allCatch.opt{FileUtils.deleteDirectory(new File(dir))}
      }

    }

    override def before {
      if (owner)  cleanup()
    }
    
    override def after {
      super.after
      if (owner)  {
        cleanup()
        system.shutdown
      }
    }
  }
}
