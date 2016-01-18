import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.tasks._
import models.tasks.Tasks._
import org.apache.commons.codec.binary.Base64

class RestSpec extends Specification {
  def encodeBase64(s: String) = new String(Base64.encodeBase64(s.getBytes))
  val auth = "Basic " + encodeBase64("admin@a.pp:$1amn_$2pwrt")

  "Application Course API" should {
    "create Course" in new WithApplication {
      val json: JsValue = Json.parse("""{"id": 100001, "title": "Neuer Kurs 5","chapters": [{"id": 1,"title": "About Scala Lists","tasks": [{"id": "video1","tag": "video-task","data": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"},"solution": "watched"},{"id": "koan1","tag": "koan-task","data": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\nval l2 = l.map { x => x + 1 }\nval l3 = l.map { x => x * x }\n\nl should be (__)\nl2 should be(__)\nl3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]},"solution": "List(1, 2, 3, 4),List(2, 3, 4, 5),List(1, 4, 9, 16)"},{"id": "koan2","tag": "koan-task","data": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]},"solution": "List(1, 3, 5, 6)"},{"id": "code1","tag": "code-task","data": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>", "mode":"scala", "code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}"},"solution": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"},{"id": "koan3","tag": "koan-task","data": {"description": "Java Koan", "mode":"java","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n\t@Koan\n\tpublic void test() {\n\t\tint i= 10;\n\t\tint j = 5;\n\t\tint product = i * j;\n\n\t\tassertThat(product, is(__)\n\t}\n}","solutions": ["50"]},"solution": "50"}]}]}""")
      val request = FakeRequest(POST, "/api/courses")
          .withJsonBody(json)
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(CREATED)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
      header("Location", result.get).get must contain("/api/courses/100001")
    }
    "get Course" in new WithApplication {
      val request = FakeRequest(GET, "/api/courses/100001")
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "update Course" in new WithApplication {
      val json: JsValue = Json.parse("""{"id": 100001, "title": "Neuer Kurs 5","chapters": [{"id": 1,"title": "About Scala Lists","tasks": [{"id": "video1","tag": "video-task","data": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"},"solution": "watched"},{"id": "koan1","tag": "koan-task","data": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\nval l2 = l.map { x => x + 1 }\nval l3 = l.map { x => x * x }\n\nl should be (__)\nl2 should be(__)\nl3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]},"solution": "List(1, 2, 3, 4),List(2, 3, 4, 5),List(1, 4, 9, 16)"},{"id": "koan2","tag": "koan-task","data": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]},"solution": "List(1, 3, 5, 6)"},{"id": "code1","tag": "code-task","data": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>", "mode":"scala", "code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}"},"solution": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"},{"id": "koan3","tag": "koan-task","data": {"description": "Java Koan", "mode":"java","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n\t@Koan\n\tpublic void test() {\n\t\tint i= 10;\n\t\tint j = 5;\n\t\tint product = i * j;\n\n\t\tassertThat(product, is(__)\n\t}\n}","solutions": ["50"]},"solution": "50"}]}]}""")
      val request = FakeRequest(PUT, "/api/courses/100001")
          .withJsonBody(json)
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "delete Course" in new WithApplication {
      val request = FakeRequest(DELETE, "/api/courses/100001")
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "get all Course" in new WithApplication {
      val request = FakeRequest(GET, "/api/courses/all")
      val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
  }

  "Application User API" should {
    "create User" in new WithApplication {
      val json: JsValue = Json.parse("""{"username": "test@test.de", "authority": "student", "password": "test", "chapterSolutions": [], "subscriptions": []}""")
      val request = FakeRequest(POST, "/api/users")
          .withJsonBody(json)
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(CREATED)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
      header("Location", result.get).get must contain("/api/users/200002")
    }
    "get User" in new WithApplication {
      val request = FakeRequest(GET, "/api/users/200002")
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "update User" in new WithApplication {
      val json: JsValue = Json.parse("""{"username": "test@test.de", "authority": "student", "password": "test", "chapterSolutions": [], "subscriptions": []}""")
        val request = FakeRequest(PUT, "/api/users/200002")
          .withJsonBody(json)
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "delete User" in new WithApplication {
      val request = FakeRequest(DELETE, "/api/users/200002")
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "get all Users" in new WithApplication {
      val request = FakeRequest(GET, "/api/users/all")
          .withHeaders(("Authorization", auth))
      val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
    "get all Students" in new WithApplication {
      val request = FakeRequest(GET, "/api/users/students")
          .withHeaders(("Authorization", auth))
      val result = route(request)

      status(result.get) must equalTo(OK)
      contentAsString(result.get) must contain("\"status\":\"OK\"")
    }
  }
}