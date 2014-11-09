package akka.testkit.specs2

import org.specs2.specification.Fragments
import org.specs2.specification.Step
import org.specs2.mutable.SpecificationLike


trait DeactivatedTimeConversions extends org.specs2.time.TimeConversions {
  override def intToRichLong(v: Int) = super.intToRichLong(v)
}
trait BeforeAndAfterAll extends SpecificationLike {
  // see http://bit.ly/11I9kFM (specs2 User Guide)
  override def map(fragments: =>Fragments) = 
    Step(beforeAll) ^ fragments ^ Step(afterAll)

  protected def beforeAll() = ()
  protected def afterAll() = ()
}