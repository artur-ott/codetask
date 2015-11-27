import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.CodeTask
import models.Execution
import models._

class UserServiceSpec extends Specification {
	val userService = new UserService(Config);
	val user1 = new User("email", "student", "pw1234", List())

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
	"UserService#update" should {
		"work with existing user" in {
			user1.authority = "teacher"
			user1.courses = List("java", "scala")
			userService.update(user1)
			val user = userService.findOneByUsername("email").get
			user.authority equals "teacher" must beTrue
			user.courses should be equalTo(List("java", "scala"))
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