import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.tasks._

class CourseServiceSpec extends Specification {
  val loop = 500

  val courseService = new CourseService(Config);
  val course1 = new Course(Course.NEW, "scala", List(
    new Chapter(1, "First Chapter", List(
      new Task("koan1", "koan-task", VideoData("video", "url"), Some("false"))
    ))
  ))

  var courseId:Long = 0

  "CourseService#findOneByTitle" should {
    "fail with unknown course name" in {
      val course = courseService.findOneByTitle("no name")
      course should be(None)
    }
  }
  "CourseService#create" should {
    "succeed when course doesn't exist" in {
      val course = courseService.create(course1)
      course should be equalTo(Some(course1))
    }
    "fail when course does exist" in {
      courseService.create(course1) should be(None)
    }
  }
  "CourseService#findOneByTitle" should {
    "find course after creation" in {
      val course = courseService.findOneByTitle(course1.title)
      course.get.id must equalTo(course1.id)
    }
  }
  "CourseService#findOneById" should {
    "find course after creation" in {
      val course = courseService.findOneById(course1.id)
      course.get.title must equalTo(course1.title)
    }
  }
  "CourseService#findAll" should {
    "give all courses" in {
      courseService.findAll().size > 0 shouldEqual(true)
    }
  }
  "CourseService#update" should {
    "work with existing course" in {
      val course2 = new Course(course1.id, "scala", List(
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", VideoData("video", "url"), Some("false"))
        )),
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", VideoData("video", "url2"), Some("false"))
        ))
      ))
      courseService.update(course2)

      for (_ <- 1 to loop) {
        val course = courseService.findOneByTitle("scala")
        course.isDefined shouldEqual true
        val x = course.get.chapters(0)
        course.get.title shouldEqual("scala")
        x.tasks(0).taskData shouldEqual(VideoData("video", "url"))
        val y = course.get.chapters(1)
        y.tasks(0).taskData shouldEqual(VideoData("video", "url2"))
      }
      val course = courseService.findOneByTitle("scala").get
      course.chapters.size must be equalTo(2)
      course.chapters(0).tasks(0).toString contains("false") must beTrue
    }
    "fail with non existing course" in {
      val result = courseService.update(new Course(0, "title", List()))
      result.isDefined must beFalse
    }
  }
  "CourseService#delete" should {
    "succeed with existing course" in {
      var course = courseService.findOneByTitle("scala")
      courseService.delete(course.get)
      courseService.findOneByTitle("scala") should be(None)
    }
  }
}