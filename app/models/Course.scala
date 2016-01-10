package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import Services._

case class Course(var id: Long, var title: String, var chapters: List[Chapter])
case class Chapter(id: Long, title: String, tasks: List[Task])
case class Task(id: String, tag: String, data: String, ext: Option[String] = None)

object Course {
  implicit val taskReads: Reads[Task] = (
    (__ \ "id").read[String] and
    (__ \ "tag").read[String] and
    (new Reads[String] {
      def reads(js: JsValue): JsResult[String] = {
        (js \ "data") match {
          case JsDefined(data) => JsSuccess(data.toString)
          case _: JsUndefined => JsError("missing data")
        }
      }
    }) and
    (__ \ "ext").readNullable[String]
  )(Task.apply _)

  implicit val chapterReads: Reads[Chapter] = (
    (__ \ "id").read[Long] and
    (__ \ "title").read[String] and
    (__ \ "tasks").read[List[Task]]
  )(Chapter.apply _)

  implicit val courseReads: Reads[Course] = (
    (__ \ "id").read[Long] orElse Reads.pure(courseService.getId()) and
    (__ \ "title").read[String] and
    (__ \ "chapters").read[List[Chapter]]
  )(Course.apply _)

  implicit val taskWrites: Writes[Task] = (
    //(__ \ "ext").format(new OWrites[String] { def writes(ext: String): JsObject = new JsString("").as[JsObject] }) and
    (__ \ "id").write[String] and
    (__ \ "tag").write[String] and
    (__ \ "data").format(new OWrites[String] {
      def writes(data: String): JsObject = {
        // fails on empty data -> prevented by Reads
        Json.parse(data).as[JsObject]
      }
    }) and
    (new OWrites[Option[String]] {
      def writes(ext: Option[String]): JsObject = {
        new JsObject(Map())
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