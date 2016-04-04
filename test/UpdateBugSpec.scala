import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.Services._

class UpdateBugSpec extends Specification {
  "courseServiceUpdate" should {
    "update" in new WithApplication {
      val course = Course(-1, "Test5", List(), None)
      var result = courseService.create(course)
      result.isEmpty shouldEqual false
      val courseInfo = CourseInfo("ZaruDan", "CodeTaskCourses", "/") 
      val githubCourse = CourseParser.parseFromGithub(courseInfo, course.title)
      githubCourse.id = result.get.id
      result = courseService.update(githubCourse)
      result.isEmpty shouldEqual false
      result = courseService.delete(githubCourse)
      result.isEmpty shouldEqual false
    }
  }
}