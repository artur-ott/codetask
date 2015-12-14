import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class TestSpec extends Specification {
	//val user1 = new User("email", "student", "pw1234", Map())

	"UserService#update" should {
		"work with existing user" in {
			/*user1.authority = "teacher"
			val jsVal = Json.parse("1")
			val seq = Map(("state" -> jsVal))
			val jsObj = new JsObject(seq)
			user1.courses = Map(("course1" -> Map("chapter1" -> Map("task1" -> jsObj, "task2" -> jsObj),
												  "chapter2" -> Map("task1" -> jsObj),
												  "chapter3" -> Map("task1" -> jsObj),
												  "chapter4" -> Map("task1" -> jsObj))))

			val chapters = user1.courses("course1").map { chapter =>
				//val tasks = chapter._2.map { task => (task._1, task._2) }
				JsObject(Seq(
					"title" -> JsString(chapter._1),
					"tasks" -> JsObject(chapter._2)
				))
			}
			val result = JsObject(Seq(
				"course" -> JsObject(Seq(
					"title" -> JsString("course1"),
					"chapters" -> JsArray(chapters.toList)
				))
			))

			println(result.toString)*/

			true must beTrue
		}
	}
}