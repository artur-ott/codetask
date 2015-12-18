import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class UserServiceSpec extends Specification {
  val userService = new UserService(Config);

  val id = userService.newId()
  val user1 = new User(id, "email", "student", "pw1234", List(
    new ChapterState(100001, 2, List(
      new TaskState("koan-task1", "{\"checked\":true}")
    )),
    new ChapterState(100002, 2, List(
      new TaskState("koan-task1", "{\"checked\":true}")
    ))
  ))

  "UserService#findOneByUsername" should {
    "fail with unknown username" in {
      val user = userService.findOneByUsername("no name")
      user should be(None)
    }
  }
  "UserService#create" should {
    "succeed when user doesn't exist" in {
      userService.create(user1) should be equalTo(Some(user1))
    }
    "fail when user does exist" in {
      userService.create(user1) should be(None)
    }
  }
  "UserService#findOneByUsername" should {
    "find user after creation" in {
      val user = userService.findOneByUsername(user1.username)
      user.get.id must equalTo(user1.id)
    }
  }
  "UserService#findOneById" should {
    "find user after creation" in {
      val user = userService.findOneById(user1.id)
      user.get.username must equalTo(user1.username)
    }
  }
  "UserService#findAll" should {
    "give all Users" in {
      userService.findAll().size shouldEqual(1)
    }
  }
  "UserService#update" should {
    "work with existing user" in {
      val tsk = new TaskState("koan-task1", "{\"checked\":false}")
      var chpt = new ChapterState(100001, 1, List(tsk))

      // replace -> 100001, 1
      val chapterStates = chpt :: user1.chapterStates.filter {
        x => x.courseId == chpt.courseId && x.chapterId == chpt.chapterId
      }

      val userNew = new User(user1.id, user1.username, "teacher",
        user1.password, chapterStates)
      userService.update(userNew)

      val user = userService.findOneByUsername(userNew.username)
      user.isDefined must beTrue
      user.get.authority equals "teacher" must beTrue
      
      val chapterState = user.get.chapterStates.find(x => x.courseId == 100001 && x.chapterId == 1)
      chapterState.isDefined must beTrue
      val taskState = chapterState.get.taskStates.find(x => x.taskId == "koan-task1")
      taskState.isDefined must beTrue
      println(taskState.get.state)
      taskState.get.state.contains("false") must beTrue
    }
    "fail without existing user" in {
      val result = userService.update(new User(0, "user", "teacher", "pw", List()))
      result.isDefined must beFalse
    }
  }
  "UserService#delete" should {
    "succeed with existing user" in {
      var user = userService.findOneByUsername("email")
      userService.delete(user.get)
      userService.findOneByUsername("email") should be(None)
    }
  }
}