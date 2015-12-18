package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import Services._

case class Course(id: Long, title: String, chapters: List[Chapter])
case class Chapter(id: Long, title: String, tasks: List[Task])
case class Task(id: String, tag: String, data: String)

object Course {
  implicit val TaskReads: Reads[Task] = (
    (__ \ "id").read[String] and
    (__ \ "tag").read[String] and
    new Reads[String] {
      def reads(js: JsValue): JsResult[String] = {
        (js \ "data") match {
          case JsDefined(data) => JsSuccess(data.toString)
          case _: JsUndefined => JsError("missing data")
        }
      }
    }
  )(Task.apply _)

  implicit val ChapterReads: Reads[Chapter] = (
    (__ \ "id").read[Long] and
    (__ \ "title").read[String] and
    (__ \ "tasks").read[List[Task]]
  )(Chapter.apply _)

  implicit val CourseReads: Reads[Course] = (
    (__ \ "id").read[Long] orElse Reads.pure(courseService.getId()) and
    (__ \ "title").read[String] and
    (__ \ "chapters").read[List[Chapter]]
  )(Course.apply _)

  def progressOf(course: Course): Int = {
    var checkNum: Double = 0
  	var taskNum: Double = 0
    course.chapters.foreach { chapter =>
      chapter.tasks.foreach { task => 
      	try {
          if ((Json.parse(task.data) \ "checked").as[Boolean]) checkNum += 1	
      	} catch {
      	  case _: Exception =>
      	}
      }
      taskNum += chapter.tasks.size
    }
    ((checkNum / taskNum) * 100).toInt
  }
}