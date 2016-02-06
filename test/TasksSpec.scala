import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.tasks._
import models.tasks.Tasks._

class TaskSpec extends Specification {
  "Tasks#TaskState" should {
    "should cast into VideoStateReads" in {
      val json = Json.parse("""{ "status": "watched" }""")
      val v = Json.fromJson[TaskState](json).get match {
        case state: VideoState => state
        case _ => VideoState("not watched")
      }
      v shouldEqual(VideoState("watched"))
    }
    "should work with VideoStateWrites" in {
      val vd: TaskState = new VideoState("not watched")
      val json = Json.toJson(vd)
      json.toString shouldEqual("{\"status\":\"not watched\"}")
    }
  }
  "Tasks#Types" should {
    "should validate VideoState" in {
     val json = Json.parse("""{ "status": "watched" }""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateState(json) match {
          case Some(taskState) => 
            taskState match {
              case state: VideoState => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
    "should validate KoanState" in {
     val json = Json.parse("""{ "mySolutions": ["eins","zwei","drei"] }""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateState(json) match {
          case Some(taskState) => 
            taskState match {
              case state: KoanState => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
    "should validate CodeState" in {
     val json = Json.parse("""{ "myCode": "some code"}""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateState(json) match {
          case Some(taskState) => 
            taskState match {
              case state: CodeState => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
    "should validate VideoData" in {
      val json = Json.parse("""{ "description": "was", "url": "url" }""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateData(json) match {
          case Some(taskData) => 
            taskData match {
              case state: VideoData => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
    "should validate KoanData" in {
      val json = Json.parse("""{"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __", "mode":"scala", "solutions": ["List(1, 3, 5, 6)"]}""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateData(json) match {
          case Some(taskData) => 
            taskData match {
              case state: KoanData => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
    "should validate CodeData" in {
      val json = Json.parse("""{ "description": "desc", "mode": "scala", "code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}"}""")
      var result = 2

      Tasks.types.foreach { t => 
        t.validateData(json) match {
          case Some(taskData) => 
            taskData match {
              case state: CodeData => result = 1
              case _ =>
            }
          case None =>
        }
      }
      result shouldEqual 1
    }
  }
}