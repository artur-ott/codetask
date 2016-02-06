package models.tasks

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class KoanState(val mySolutions: List[String]) extends TaskState {
  def toJson = JsObject(Map("mySolutions" -> Json.toJson(mySolutions)))
  
  def isSolved(solution: String): Boolean = {
    mySolutions.mkString(",") == solution
  }
}

case class KoanData(val description: String, val code: String, val mode: String, val solutions: List[String]) extends TaskData {
  // nullpointer exception in 53 ?
  def toJson = Json.obj("description" -> description,
                        "code"        -> code,
                        "mode"        -> mode,
                        "solutions"   -> solutions)
}

object KoanTask extends TaskType {
  val koanStateReads: Reads[KoanState] = 
    (__ \ "mySolutions").read[List[String]].map{s => if (s == null) { play.Logger.info("solution null") }; KoanState(s)}

  val koanDataReads: Reads[KoanData] = (
    (__ \ "description").read[String] and
    (__ \ "code").read[String] and
    (__ \ "mode").read[String] and
    (__ \ "solutions").read[List[String]]
  )(KoanData.apply _)
  
  def validateState(js: JsValue): Option[TaskState] = {
    js.validate[KoanState](koanStateReads).fold(
      {errors => None},
      {KoanState => Some(KoanState)}
    )
  }
  def validateData(js: JsValue): Option[TaskData] = {
    js.validate[KoanData](koanDataReads).fold(
      {errors => None},
      {KoanData => Some(KoanData)}
    )
  }

}
