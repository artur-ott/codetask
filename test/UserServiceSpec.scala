import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class UserServiceSpec extends Specification {
	val userService = new UserService(Config);
	val user1 = new User("email", "student", "pw1234", Map())

	"UserService#getOneByUsername" should {
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
	"UserService#findAll" should {
		"give all Users" in {
			userService.findAll().size shouldEqual(1)
		}
	}
	"UserService#update" should {
		"work with existing user" in {
			user1.authority = "teacher"
			val jsVal = Json.parse("1")
			val seq = Map(("state" -> jsVal))
			val jsObj = new JsObject(seq)
			user1.courses = Map(("course1" -> Map("chapter1" -> Map("task1" -> jsObj))))
			userService.update(user1)
			val user = userService.findOneByUsername("email").get
			user.authority equals "teacher" must beTrue
			user.courses.contains("course1") must beTrue
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