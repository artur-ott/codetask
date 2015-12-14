import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class UserServiceSpec extends Specification {
	val userService = new UserService(Config);

	val id = userService.getId()
	val user1 = new User(id, "email", "student", "pw1234")

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
			user1.authority = "teacher"
			user1.courses = Map(("course1" -> Map("chapter1" -> "json")))
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