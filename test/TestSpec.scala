import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._

class TestSpec extends Specification {
  val id = 0;
  val course0 = new Course(id, "scala", List(
    new Chapter(1, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":false}")
    ))
  ))

  val course50 = new Course(id, "scala", List(
    new Chapter(1, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":false}")
    )),
    new Chapter(2, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":true}")
    ))
  ))

  val course33 = new Course(id, "scala", List(
    new Chapter(1, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":false}")
    )),
    new Chapter(2, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":false}")
    )),
    new Chapter(3, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":true}")
    ))
  ))

  val course100 = new Course(id, "scala", List(
    new Chapter(1, "First Chapter", List(
      new Task("koan1", "koan-task", "{\"checked\":true}")
    ))
  ))

  val course = new Course(id, "scala", List(
    new Chapter(1, "First Chapter", List())
  ))


  "Course#progressOf" should {
    "be 0% for course0" in {
      val completion = Course.progressOf(course0)
      completion should equalTo(0)
    }
    "be 50% for course50" in {
      val completion = Course.progressOf(course50)
      completion should equalTo(50)
    }
    "be 33% for course33" in {
      val completion = Course.progressOf(course33)
      completion should equalTo(33)
    }
    "be 100% for course100" in {
      val completion = Course.progressOf(course100)
      completion should equalTo(100)
    }
    "be 0% for course" in {
      val completion = Course.progressOf(course0)
      completion should equalTo(0)
    }
  }
}