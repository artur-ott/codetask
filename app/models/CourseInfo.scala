package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class CourseInfo(var githubUser: String, var githubRepo: String, var path: String)

object CourseInfo {
    implicit val courseReads: Reads[CourseInfo] = (
      (__ \ "githubUser").read[String] and
      (__ \ "githubRepo").read[String] and
      (__ \ "path").read[String]
  )(CourseInfo.apply _)

    implicit val courseWrites: Writes[CourseInfo] = (
      (__ \ "githubUser").write[String] and
      (__ \ "githubRepo").write[String] and
      (__ \ "path").write[String]
  )(unlift(CourseInfo.unapply))
}
