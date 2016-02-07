package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import Services._
import models.tasks._
import models.tasks.Tasks._

case class Course(var id: Long, var title: String, var chapters: List[Chapter])
case class Chapter(id: Long, title: String, tasks: List[Task])
case class Task(id: String, tag: String, taskData: TaskData, solution: Option[String] = None)

object Course {
  def NEW: Long = -1

  implicit val taskReads: Reads[Task] = (
    (__ \ "id").read[String] and
    (__ \ "tag").read[String] and
    (__ \ "data").read[TaskData] and
    (__ \ "solution").readNullable[String]
  )(Task.apply _)

  implicit val chapterReads: Reads[Chapter] = (
    (__ \ "id").read[Long] and
    (__ \ "title").read[String] and
    (__ \ "tasks").read[List[Task]]
  )(Chapter.apply _)

  implicit val courseReads: Reads[Course] = (
    (__ \ "id").read[Long] orElse Reads.pure(NEW) and
    (__ \ "title").read[String] and
    (__ \ "chapters").read[List[Chapter]]
  )(Course.apply _)

  implicit val taskWrites: Writes[Task] = (
    (__ \ "id").write[String] and
    (__ \ "tag").write[String] and
    (__ \ "data").write[TaskData] and
    (new OWrites[Option[String]] {
      def writes(solution: Option[String]): JsObject = {
        Json.obj()
      }
    })
  )(unlift(Task.unapply))

  implicit val chapterWrites: Writes[Chapter] = (
    (__ \ "id").write[Long] and
    (__ \ "title").write[String] and
    (__ \ "tasks").write[List[Task]]
  )(unlift(Chapter.unapply))

  implicit val courseWrites: Writes[Course] = (
    (__ \ "id").write[Long] and
    (__ \ "title").write[String] and
    (__ \ "chapters").write[List[Chapter]]
  )(unlift(Course.unapply))
}