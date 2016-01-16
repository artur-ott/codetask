package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.tasks.Tasks._
import models.tasks._

// courses: Map[coursename, Map[chaptername, Map[taskname, solution]]]
case class User(
  id: Long,
  username: String,
  var authority: String,
  var password: String,
  var chapterSolutions: List[ChapterSolution] = List(),
  var subscriptions: Set[Long] = Set()
)

case class ChapterSolution(courseId: Long, chapterId: Long, taskSolutions: List[TaskSolution])
//case class TaskSolution(taskId: String, state: String) // checked: Boolean = false)
case class TaskSolution(taskId: String, taskState: TaskState, var checked: Option[Boolean] = Some(false))

object User {
  implicit val taskSolutionReads: Reads[TaskSolution] = (
    (__ \ "taskId").read[String] and
    (__ \ "taskState").read[TaskState] and
    (__ \ "checked").readNullable[Boolean]
  )(TaskSolution.apply _)

  implicit val chapterSolutionReads: Reads[ChapterSolution] = (
    (__ \ "courseId").read[Long] and
    (__ \ "chapterId").read[Long] and
    (__ \ "taskSolutions").read[List[TaskSolution]]
  )(ChapterSolution.apply _)

  implicit val taskSolutionWrites: Writes[TaskSolution] = (
    (__ \ "taskId").write[String] and
    (__ \ "taskState").write[TaskState] and
    (__ \ "checked").writeNullable[Boolean]
  )(unlift(TaskSolution.unapply))

  implicit val chapterSolutionWrites: Writes[ChapterSolution] = (
    (__ \ "courseId").write[Long] and
    (__ \ "chapterId").write[Long] and
    (__ \ "taskSolutions").write[List[TaskSolution]]
  )(unlift(ChapterSolution.unapply))

  def progressOf(course: Course, chapterSolutions: List[ChapterSolution]): Int = {
    var checks = 0.0
    var sum = 0.0
    val sizes = course.chapters.foreach { chapter => sum += chapter.tasks.size }

    chapterSolutions.foreach { chapterSolution => 
      chapterSolution.taskSolutions.foreach { taskSolution => 
        taskSolution.checked match {
          case Some(true) => checks += 1
          case _ =>
        }
      }
    }

    if (sum > 0) ((checks / sum) * 100).toInt else 0
  }
}