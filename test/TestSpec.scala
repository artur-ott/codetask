import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class TestSpec extends Specification {
	val user1 = new User("email", "student", "pw1234", Map())

	"UserService#update" should {
		"work with existing user" in {
			user1.authority = "teacher"
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

			println(result.toString)

			true must beTrue
		}
	}

	val courseService = new CourseService(Config);
	val jsObj = Json.parse("""{ "course": {
        "title": "scala1",
        "chapters": [{
        "title": "About Scala Lists",
        "tasks": {
            "video1": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"},
            "koan1": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\n    val l2 = l.map { x => x + 1 }\n    val l3 = l.map { x => x * x }\n    \n    l should be (__)\n    l2 should be(__)\n    l3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]},
            "koan2": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]},
            "codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>","code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"},
            "koan3": {"description": "Java Koan","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n    @Koan\n    public void test() {\n      int i= 10;\n      int j = 5;\n      int product = i * j;\n\n      assertThat(product, is(__)\n    }\n  }","solutions": ["50"]}
        }
    }]
    }
}""").asInstanceOf[JsObject]

	val course1 = new Course("scala1", jsObj)


	"CourseService#create" should {
		"succeed when course doesn't exist" in {
			courseService.create(course1) should be equalTo(Some(course1))
		}
	}

}