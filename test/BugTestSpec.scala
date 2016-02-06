import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.tasks._
import models.Services._

class BugTestSpec extends Specification {

  "CourseService" should {
    "not find null elements" in {

      var list:List[String] = Nil

      courseService.findAll().foreach { course => 
        if (course == null) list = list ++ List("course null")
        else if (course.chapters == null) list = list ++ List("course " + course.id + "chapters null")
        course.chapters.foreach { chapter =>
          if (chapter == null) list = list ++ List("course " + course.id + " chapter null")
          else if (chapter.tasks == null) list = list ++ List("course " + course.id + " chapter " + chapter.id + " tasks null")
          chapter.tasks.foreach { task => 
            if (task == null) list = list ++ List("course " + course.id + "chapter " + chapter.id + " task null")
            else if (task.data == null) list = list ++ List("course " + course.id + "chapter " + chapter.id + " task " + task.id + " task.data null")
            else if (task.solution == null) list = list ++ List("course " + course.id + "chapter " + chapter.id + " task " + task.id + " task.solution null")
          }
        }
      }

      list shouldEqual Nil
    }
  }
  
  "UserService" should {
    "not find null elements" in {

      var list:List[String] = Nil

      userService.findAll().foreach { user => 
        if (user == null) list = list ++ List("course null")
        else if (user.chapterSolutions == null) list = list ++ List("user " + user.id + "chapters null")
        user.chapterSolutions.foreach { chapterSolution =>
          if (chapterSolution == null) list = list ++ List("user " + user.id + " chapter null")
          else if (chapterSolution.taskSolutions == null) list = list ++ List("user " + user.id + "chapter " + chapterSolution.chapterId + " course " + chapterSolution.courseId + " taskSolutions null")
          chapterSolution.taskSolutions.foreach { taskSolution => 
            if (taskSolution == null) list = list ++ List("user " + user.id + "chapter " + chapterSolution.chapterId + " course " + chapterSolution.courseId + " taskSolution null")
            else if (taskSolution.taskState == null) list = list ++ List("user " + user.id + "chapter " + chapterSolution.chapterId + " course " + chapterSolution.courseId + " task " + taskSolution.taskId + " taskSolution.taskState null")
            else if (taskSolution.checked == null) list = list ++ List("user " + user.id + "chapter " + chapterSolution.chapterId + " course " + chapterSolution.courseId + " task " + taskSolution.taskId + " taskSolution.checked null")
          }
        }
      }

      list shouldEqual Nil
    }
  }
}