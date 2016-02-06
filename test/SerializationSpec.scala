import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.User._
import models.Course._
import models.tasks.Tasks._
import models.tasks._
import com.db4o.Db4o
import models.db4o.Util.objCont2QueryHelper
import models.db4o.Util.objectServer

class SerializationSpec extends Specification {
  "UserRepositoryDb4o" should {
    "serialize KoanState" in {
      val taskState: TaskState = KoanState(List("eins", "zwei", "drei"))
      val client = objectServer.openClient()
      client.store(taskState)
      client.commit()
      var readState: Option[TaskState] = None
      client query {s: KoanState => s.mySolutions.head == "eins"} match {
        case h :: t => readState = Some(h)
        case _ => readState = None
      }
      client.close()
      readState.get.toJson.toString shouldEqual("{\"mySolutions\":[\"eins\",\"zwei\",\"drei\"]}")
    }
    "serialize List" in {
      val list = List("eins", "zwei", "drei")
      val client = objectServer.openClient()
      client.store(list)
      client.commit()
      var l: List[String] = null
      client query {s: List[String] => s.head == "eins"} match {
        case h :: t => l = h
        case _ => 
      }
      client.close()
      l shouldEqual(List("eins", "zwei", "drei"))
    }
  }
}