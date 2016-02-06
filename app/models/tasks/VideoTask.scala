package models.tasks

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class VideoState(status: String = "not watched") extends TaskState {
  def toJson = Json.obj("status" -> status)
  def isSolved(solution: String): Boolean = status == solution
}

case class VideoData(description: String, url: String) extends TaskData {
  def toJson = Json.obj("description" -> description,
                        "url"         -> url)
}

object VideoTask extends TaskType {
  val videoStateReads: Reads[VideoState] = 
    (__ \ "status").read[String].map{s => VideoState(s)}

  val videoDataReads: Reads[VideoData] = (
    (__ \ "description").read[String] and
    (__ \ "url").read[String]
  )(VideoData.apply _)
  
  def validateState(js: JsValue): Option[TaskState] = {
    js.validate[VideoState](videoStateReads).fold(
      {errors => None},
      {videoState => Some(videoState)}
    )
  }
  def validateData(js: JsValue): Option[TaskData] = {
    js.validate[VideoData](videoDataReads).fold(
      {errors => None},
      {videoData => Some(videoData)}
    )
  }

}
