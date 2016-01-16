import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.tasks._
import models.tasks.Tasks._

class TaskSpec extends Specification {
  "Tasks#VideoState" should {
    "VideoStateReads" in {
      val json = """{ "status": "watched" }"""
      val v = Json.fromJson[TaskState](Json.parse(json).as[JsObject]).get match {
        case state: VideoState => state
        case _ => VideoState("not watched")
      }
      v should be(VideoState("watched"))
    }
    "VideoStateWrites" in {
      val vd: TaskState = new VideoState("not watched")
      val json = Json.toJson(vd)
      vd should be(JsObject(Map("status" -> JsString("not watched"))))
    }
  }
  "Tasks#VideoData" should {
    "VideoDataReads" in {
     val json = Json.toJson("""{"status":"watched"}""").as[JsObject]
      //val json = new JsObject(Map("status" -> JsString("watched")))
      var result = 2

      Tasks.types.foreach { t => 
        result = t.validateState(json) match {
          case Some(taskState) => 1
          case None => 2
        }
      }
      result shouldEqual 1
    }
    "VideoDataWrites" in {
      val json = Json.toJson("""{"data":{ "description": "was", "url": "url" }}""")
      var result = 2

      Tasks.types.foreach { t => 
        result = t.validateData(json) match {
          case Some(taskData) => 1
          case None => 2
        }
      }
      result shouldEqual 1
    }
  }
}