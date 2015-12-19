import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.User._

class TestSpec extends Specification {
  "Course#progressOf" should {
    "be 0% for course0" in {
      val course0 = new Course(100001, "scala", List(
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", "data")))))

      val chapterStates0 = List(
        new ChapterState(100001, 1, List(
          new TaskState("koan1", "{\"checked\":false}"))))

      val completion = progressOf(course0, chapterStates0)
      completion should equalTo(0)
    }
    "be 50% for course50" in {
      val course50 = new Course(100001, "scala", List(
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", "data"),
          new Task("koan2", "koan-task", "data")
        ))
      ))

      val chapterStates50 = List(
        new ChapterState(100001, 1, List(
          new TaskState("koan1", "{\"checked\":false}"),
          new TaskState("koan2", "{\"checked\":true}"))))

      val completion = progressOf(course50, chapterStates50)
      completion should equalTo(50)
    }
    "be 33% for course33" in {
      val course33 = new Course(100001, "scala", List(
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", "data")
        )),
        new Chapter(2, "First Chapter", List(
          new Task("koan1", "koan-task", "data")
        )),
        new Chapter(3, "First Chapter", List(
          new Task("koan1", "koan-task", "data")
        ))
      ))

      val chapterStates33 = List(
        new ChapterState(100001, 3, List(
          new TaskState("koan1", "{\"checked\":true}")
        ))
      )

      val completion = progressOf(course33, chapterStates33)
      completion should equalTo(33)
    }
    "be 100% for course100" in {
      val course100 = new Course(100001, "scala", List(
        new Chapter(1, "First Chapter", List(
          new Task("koan1", "koan-task", "data")
        ))
      ))

      val chapterStates100 = List(
        new ChapterState(100001, 1, List(
          new TaskState("koan1", "{\"checked\":true}")
        ))
      )

      val completion = progressOf(course100, chapterStates100)
      completion should equalTo(100)
    }
    "be 0% for course" in {
      val course = new Course(100001, "scala", List(
        new Chapter(1, "First Chapter", List())
      ))

      val chapterStates = List(
        new ChapterState(100001, 1, List())
      )

      val completion = progressOf(course, chapterStates)
      completion should equalTo(0)
    }
  }
}