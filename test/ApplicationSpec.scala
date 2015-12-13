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

    "send console output" in new WithApplication {
      val out = route(FakeRequest(GET, "/testInterpret/println(1+41)")).get
      contentType(out) must beSome.which(_ == "text/plain")
      contentAsString(out) must contain ("42")
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

    "create course" in new WithApplication {
        // create course
        val json: JsValue = Json.parse("""{ "course": {"title": "scala1","chapters": [{"title": "About Scala Lists","tasks": {"video1": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"},"koan1": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\n    val l2 = l.map { x => x + 1 }\n    val l3 = l.map { x => x * x }\n    \n    l should be (__)\n    l2 should be(__)\n    l3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]},"koan2": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]},"codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>","code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"},"koan3": {"description": "Java Koan","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n    @Koan\n    public void test() {\n      int i= 10;\n      int j = 5;\n      int product = i * j;\n\n      assertThat(product, is(__)\n    }\n  }","solutions": ["50"]}}}]}}""")

        val request = FakeRequest(POST, "/course/json/ScalaForBeginners")
          .withJsonBody(json)
          .withSession("username" -> "admin@test.de", "password" -> "test")
        val result = route(request)
        val c = Services.courseService.findOneByName("ScalaForBeginners") 

        status(result.get) must equalTo(OK)
        (c == None) must beFalse
    }

    "subscribe user to course" in new WithApplication {
      val request = FakeRequest(GET, "/course/subscribe/ScalaForBeginners")
        .withSession("username" -> "test@test.de", "password" -> "test")

      val result = route(request)
      status(result.get) must equalTo(SEE_OTHER)
      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
    }

    "unsubscribe user from course" in new WithApplication {
      val request = FakeRequest(GET, "/course/unsubscribe/ScalaForBeginners")
        .withSession("username" -> "test@test.de", "password" -> "test")

      val result = route(request)
      status(result.get) must equalTo(SEE_OTHER)
      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
    }

    /*"delete course" in new WithApplication {
        val request = FakeRequest(DELETE, "/course/json/ScalaForBeginners")
          .withJsonBody(json)
          .withSession("username" -> "admin@test.de", "password" -> "test")
        val result = route(request)
        val c = Services.courseService.findOneByName("ScalaForBeginners") 

        status(result.get) must equalTo(OK)
        (c == None) must beFalse
    }*/
  }
}
