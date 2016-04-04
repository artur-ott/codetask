import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.CourseParser
import models.CourseInfo

class CourseParserSpec extends Specification {
  "CourseParser" should {
    "parseFromGithub" in new WithApplication {
      val courseInfo = CourseInfo("ZaruDan", "CodeTaskCourses", "/")

      val course = CourseParser.parseFromGithub(courseInfo, "Komplexe Zahlen")

      course.title shouldEqual("Komplexe Zahlen")
      course.chapters.size shouldEqual(1)
    }
  }
}
