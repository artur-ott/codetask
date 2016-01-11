package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

// courses: Map[coursename, Map[chaptername, Map[taskname, solution]]]
case class User(
  id: Long,
  username: String,
  var authority: String,
  var password: String,
  var chapterStates: List[ChapterState] = List(),
  var subscriptions: Set[Long] = Set()
)

case class ChapterState(courseId: Long, chapterId: Long, taskStates: List[TaskState])
case class TaskState(taskId: String, state: String)

object User {
  implicit val taskStateReads: Reads[TaskState] = (
    (__ \ "taskId").read[String] and
    new Reads[String] {
      def reads(js: JsValue): JsResult[String] = {
        (js \ "state") match {
          case JsDefined(state) => JsSuccess(state.toString)
          case _: JsUndefined => JsError("missing data")
        }
      }
    }
  )(TaskState.apply _)

  implicit val chapterStateReads: Reads[ChapterState] = (
    (__ \ "courseId").read[Long] and
    (__ \ "chapterId").read[Long] and
    (__ \ "taskStates").read[List[TaskState]]
  )(ChapterState.apply _)

  implicit val taskStateWrites: Writes[TaskState] = (
    (__ \ "taskId").write[String] and
    (__ \ "state").format(new OWrites[String] {
      def writes(state: String): JsObject = {
        // fails on empty data -> prevented by Reads
        println("state: " + state)
        Json.parse(state).as[JsObject]
      }
    })
  )(unlift(TaskState.unapply))

  implicit val chapterStateWrites: Writes[ChapterState] = (
    (__ \ "courseId").write[Long] and
    (__ \ "chapterId").write[Long] and
    (__ \ "taskStates").write[List[TaskState]]
  )(unlift(ChapterState.unapply))

   def progressOf(course: Course, chapterStates: List[ChapterState]): Int = {
    var checks = 0.0
    var sum = 0.0
    val sizes = course.chapters.foreach { chapter => sum += chapter.tasks.size } 

    chapterStates.foreach { chapterState => 
      chapterState.taskStates.foreach { taskState => 
        try {
          if ((Json.parse(taskState.state) \ "checked").as[Boolean]) checks += 1  
          //if (taskStates.checked) checks += 1  
        } catch {
          case _: Exception =>
        }
      }
    }

    if (sum > 0) ((checks / sum) * 100).toInt else 0
  }
}