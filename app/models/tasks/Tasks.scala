package models.tasks

import play.api.libs.json._

trait TaskType {
  def validateData(js: JsValue): Option[TaskData] 
  def validateState(js: JsValue): Option[TaskState] 
}

trait TaskState {
  def toJson: JsValue
  def isSolved(solution: String): Boolean
}

trait TaskData {
  def toJson: JsValue
}

object Tasks {
  val types: List[TaskType] = List(VideoTask, KoanTask, CodeTask)

  implicit val taskDataWrites = TaskDataWrites
  implicit val taskDataReads = TaskDataReads
  implicit val taskStateWrites = TaskStateWrites
  implicit val taskStateReads = TaskStateReads
}

object TaskStateReads extends Reads[TaskState] {
  def reads(js: JsValue): JsResult[TaskState] = {
    var taskState: Option[TaskState] = None
    var i = 0
    do {
      taskState = Tasks.types(i).validateState(js)
      i += 1
    } while(taskState == None && i < Tasks.types.size)

    if (taskState == null) play.Logger.info("is null")

    taskState match {
      case Some(state) => JsSuccess(state)
      case None => JsError("Task type not found")
    }
  }
}

object TaskDataReads extends Reads[TaskData] {
  def reads(js: JsValue): JsResult[TaskData] = {
    var taskData: Option[TaskData] = None
    var i = 0;
    do {
      taskData = Tasks.types(i).validateData(js)
      i += 1
    } while(taskData == None && i < Tasks.types.size)

    if (taskData == null) play.Logger.info("is null")

    taskData match {
      case Some(data) => JsSuccess(data)
      case None => JsError("Task type not found")
    }
  }
}

object TaskStateWrites extends Writes[TaskState] {
  def writes(taskState: TaskState) = taskState.toJson
}

object TaskDataWrites extends Writes[TaskData] {
  def writes(taskData: TaskData) = taskData.toJson
}