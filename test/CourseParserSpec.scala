import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.CourseParser

class CourseParserSpec extends Specification {
  "CourseParser" should {
    "parseFromGithub" in new WithApplication {
      val url = CourseParser.mkGithubApiUrl("ZaruDan", "CodeTaskCourses", "/")
      val course = CourseParser.parseFromGithub(url, "Komplexe Zahlen")

      course.title shouldEqual("Komplexe Zahlen")
      course.chapters.size shouldEqual(1)
    }
  }
}
