import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  val id1 = Services.userService.newId()
  val id2 = Services.userService.newId()
  val user1 = new User(id1, "admin@test.de", "teacher", "test", List())
  val user2 = new User(id2, "test@test.de", "student", "test", List())
  Services.userService.create(user1)
  Services.userService.create(user2)

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/test")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/plain")
      contentAsString(home) must contain ("Your Application is ready.")
    }

    // User Actions

    "login with credentials" in new WithApplication {
        val request = FakeRequest(POST, "/authenticate").withFormUrlEncodedBody(
          "email" -> "test@test.de", 
          "password" -> "test"
        )
        val result = route(request)

        result.isDefined must beTrue
        status(result.get) must equalTo(SEE_OTHER)
        redirectLocation(result.get) must beSome.which(_ == "/dashboard")
    }

    "fail login with false credentials" in new WithApplication {
        val request = FakeRequest(POST, "/authenticate").withFormUrlEncodedBody(
          "email" -> "t@test.de", 
          "password" -> "test"
        )
        val result = route(request)

        result.isDefined must beTrue
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "create course" in new WithApplication {
        // create course
        val json: JsValue = Json.parse("""{
            "id": null,
            "title": "Scala For Beginners",
            "chapters": [
              {
                "id": 1,
                "title": "About Scala Lists",
                "tasks": [
                  {
                    "id": "video1",
                    "tag": "video-task",
                    "data": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"}
                  },{
                    "id": "koan1",
                    "tag": "koan-task",
                    "data": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\nval l2 = l.map { x => x + 1 }\nval l3 = l.map { x => x * x }\n\nl should be (__)\nl2 should be(__)\nl3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]} 
                  },{
                    "id": "koan2",
                    "tag": "koan-task",
                    "data": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]}
                  },{
                    "id": "code1",
                    "tag": "code-task",
                    "data": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>", "mode":"scala", "code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"}
                  },{
                    "id": "koan3",
                    "tag": "koan-task",
                    "data": {"description": "Java Koan", "mode":"java","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n\t@Koan\n\tpublic void test() {\n\t\tint i= 10;\n\t\tint j = 5;\n\t\tint product = i * j;\n\n\t\tassertThat(product, is(__)\n\t}\n}","solutions": ["50"]}
                  }
                ]
              },{
                "id": 2,
                "title": "Java Chapter",
                "tasks": [
                  {
                    "id": "video1",
                    "tag": "video-task",
                    "data": {"description": "First video", "url": "https://www.youtube.com/watch?v=dVFr5TETfjY"}
                  },{
                    "id": "koan1",
                    "tag": "koan-task",
                    "data": {"description": "Java Koan", "mode":"java","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n\t@Koan\n\tpublic void test() {\n\t\tint i= 10;\n\t\tint j = 5;\n\t\tint product = i * j;\n\n\t\tassertThat(product, is(__)\n\t}\n}","solutions": ["50"]}
                  }
                ]
              }
            ]
        }""")

        val request = FakeRequest(POST, "/api/courses/new")
          .withJsonBody(json)
          .withSession("username" -> "admin@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"OK\"")
        status(result.get) must equalTo(OK)
    }

    "fail with false formated course" in new WithApplication {
        // create course
        val json: JsValue = Json.parse("""{
            "id": null,
            "title": "Scala For Beginners",
            "chapters": [
              {
                "id": 1,
                "title": "About Scala Lists",
                "tasks": [
                  {
                    "id": "koan3",
                    "tag": "koan-task"
                  }
                ]
              }
            ]
        }""") // missing data prop in koan3

        val request = FakeRequest(POST, "/api/courses/new")
          .withJsonBody(json)
          .withSession("username" -> "admin@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail to create malformated course" in new WithApplication {
        // create course
        val json: JsValue = Json.parse("""{
          "course": {
            "title": "Scala For Beginners"
          }
        }""")

        val request = FakeRequest(POST, "/api/courses/new")
          .withJsonBody(json)
          .withSession("username" -> "admin@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(BAD_REQUEST)
        contentAsString(result.get) must contain("\"status\":\"KO\"")
    }


    "subscribe user to course" in new WithApplication {
      val request = FakeRequest(GET, "/subscribe/100001")
        .withSession("username" -> "test@test.de", "password" -> "test")

      val result = route(request)
      status(result.get) must equalTo(SEE_OTHER)
      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
    }


    "retreive courses json" in new WithApplication {
        val request = FakeRequest(GET, "/api/courses")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(OK)
        contentAsString(result.get) must contain("{\"id\":100001,\"title\":\"Scala For Beginners\"")
    }
//
//    "retreive course json" in new WithApplication {
//        val request = FakeRequest(GET, "/courses/100001")
//          .withSession("username" -> "test@test.de", "password" -> "test")
//        val result = route(request)
//
//        status(result.get) must equalTo(OK)
//        contentAsString(result.get) must contain("\"title\":\"Scala For Beginners\"")
//    }
//
//    "store state of chapter" in new WithApplication {
//        val json: JsValue = Json.parse("""{"title": "About Scala Lists", "states": {"koan1": {"state": {"checked": true,"mySolutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]}}}}""")
//
//        val request = FakeRequest(POST, "/solutions/100001")
//          .withJsonBody(json)
//          .withSession("username" -> "test@test.de", "password" -> "test")
//        val result = route(request)
//
//        contentAsString(result.get) must contain("{\"success\":\"state saved\"}")
//        status(result.get) must equalTo(OK)
//    }
//
//    "retreive chapter states" in new WithApplication {
//        val request = FakeRequest(GET, "/solutions/100001")
//          .withSession("username" -> "test@test.de", "password" -> "test")
//        val result = route(request)
//
//        status(result.get) must equalTo(OK)
//        contentAsString(result.get) must contain("\"mySolutions\":[\"List(1, 2, 3, 4)\"")
//    }
//
//    "unsubscribe user from course" in new WithApplication {
//      val request = FakeRequest(GET, "/unsubscribe/100001")
//        .withSession("username" -> "test@test.de", "password" -> "test")
//
//      val result = route(request)
//      status(result.get) must equalTo(SEE_OTHER)
//      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
//    }
//
//    "delete course" in new WithApplication {
//        val request = FakeRequest(DELETE, "/courses/100001")
//          .withSession("username" -> "admin@test.de", "password" -> "test")
//        val result = route(request)
//
//        status(result.get) must equalTo(OK)
//        contentAsString(result.get) must contain("{\"success\":\"course deleted\"")
//    }
  }
}
