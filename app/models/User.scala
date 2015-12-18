package models

import play.api.libs.json._
import scala.util.Random

// courses: Map[coursename, Map[chaptername, Map[taskname, solution]]]
//class User(var id: Long, var username: String, var authority: String, var password: String, var courses: Map[String, Map[String, String]] = Map())
case class User(id: Long, username: String, authority: String, password: String, chapterStates: List[ChapterState] = List())
case class ChapterState(courseId: Long, chapterId: Long, taskStates: List[TaskState])
case class TaskState(taskId: String, state: String)