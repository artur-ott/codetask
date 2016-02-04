package models.tasks

import play.api.libs.json._
import play.api.libs.functional.syntax._

object KoanTask extends TaskType {
  def validateState(js: JsValue): Option[TaskState] = {
  	println(js)
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

  val koanStateReads: Reads[KoanState] = 
    (__ \ "mySolutions").read[List[String]].map{s => KoanState(s)}

  val koanDataReads: Reads[KoanData] = (
    (__ \ "description").read[String] and
    (__ \ "code").read[String] and
    (__ \ "mode").read[String] and
    (__ \ "solutions").read[List[String]]
  )(KoanData.apply _)
}

case class KoanState(mySolutions: List[String] = List()) extends TaskState {
  def toJson = {
    var result = JsObject(Map("mySolutions" -> JsArray(Seq(JsString("")))))
    try {
      result = JsObject(Map("mySolutions" -> Json.toJson(mySolutions)))
    } catch {
      case e: java.lang.NullPointerException => play.Logger.info("could not parse solutions to Json in KoanTask.scala")
    }

    result
  }
  def isSolved(solution: String): Boolean = {
    mySolutions.mkString(",") == solution
  }
}

case class KoanData(description: String, code: String, mode: String, solutions: List[String]) extends TaskData {
  def toJson = Json.obj("description" -> description,
                        "code"        -> code,
                        "mode"        -> mode,
                        "solutions"   -> solutions)
}