import org.specs2.mutable._

import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import tasks._

class UserServiceSpec extends Specification {
  val userService = new UserService(Config);
  val loop = 500

  val user1 = new User(User.NEW, "email", "student", userService.passwordHash("pw1234"), List(
    new ChapterSolution(100001, 2, List(
      new TaskSolution("koan-task1", KoanState(List("eins", "zwei", "drei")), Some(true)),
      new TaskSolution("code-task1", CodeState("my code"), Some(true)),
      new TaskSolution("code-task1", VideoState("watched"), Some(true))
    )),
    new ChapterSolution(100002, 2, List(
      new TaskSolution("koan-task1", KoanState(List("eins", "zwei", "drei")), Some(true))
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
      for (_ <- 1 to loop) {
        val user = userService.findOneByUsername(user1.username)
        user.isDefined shouldEqual true
        user.get.username must equalTo(user1.username)
        val x = user.get.chapterSolutions(0)
        x.taskSolutions(1).taskState shouldEqual(CodeState("my code"))
        x.taskSolutions(0).taskState shouldEqual(KoanState(List("eins", "zwei", "drei")))
        x.taskSolutions(2).taskState shouldEqual(VideoState("watched"))
      }
      true shouldEqual true
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
      userService.findAll().size > 0 shouldEqual(true)
    }
  }
  "UserService#update" should {
    "work with existing user" in {
      val tsk = new TaskSolution("koan-task1", VideoState("watched"), Some(false))
      var chpt = new ChapterSolution(1, 1, List(tsk))

      // replace -> 100001, 1
      val chapterSolutions = chpt :: user1.chapterSolutions.filter {
        x => x.courseId == chpt.courseId && x.chapterId == chpt.chapterId
      }

      val userNew = new User(user1.id, user1.username, "teacher",
        user1.password, chapterSolutions)
      userService.update(userNew)

      val user = userService.findOneByUsername(userNew.username)
      user.isDefined must beTrue
      user.get.authority equals "teacher" must beTrue
      
      val chapterSolution = user.get.chapterSolutions.find(x => x.courseId == 1 && x.chapterId == 1)
      chapterSolution.isDefined must beTrue
      val taskState = chapterSolution.get.taskSolutions.find(x => x.taskId == "koan-task1")
      taskState.isDefined must beTrue
      taskState.get.checked shouldEqual(Some(false))
    }
    "fail without existing user" in {
      val result = userService.update(new User(1000, "user", "teacher", "pw", List()))
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