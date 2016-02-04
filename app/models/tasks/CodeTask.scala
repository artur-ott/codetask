package models.tasks

import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Interpreter

object CodeTask extends TaskType {
  def validateState(js: JsValue): Option[TaskState] = {
    js.validate[CodeState](codeStateReads).fold(
      {errors => None},
      {CodeState => Some(CodeState)}
    )
  }
  def validateData(js: JsValue): Option[TaskData] = {
    js.validate[CodeData](codeDataReads).fold(
      {errors => None},
      {CodeData => Some(CodeData)}
    )
  }

  val codeStateReads: Reads[CodeState] =
    (__ \ "myCode").read[String].map{ c => CodeState(c)}

  val codeDataReads: Reads[CodeData] = (
    (__ \ "description").read[String] and
    (__ \ "mode").read[String] and
    (__ \ "code").read[String]
  )(CodeData.apply _)
}

case class CodeState(myCode: String = "") extends TaskState {
  def toJson = Json.obj("myCode" -> myCode)
  def isSolved(solution: String): Boolean = {
    var solved = false
    try {
      val executeCode = myCode + "\n" + solution
      val result = Interpreter.run("scala", executeCode)
      solved = result.success
    } catch {
      case e: Exception => solved = false
    }
    solved
  }
    
}

case class CodeData(description: String, mode: String, code: String) extends TaskData {
  def toJson = Json.obj("description" -> description,
                        "mode"        -> mode,
                        "code"        -> code)
}