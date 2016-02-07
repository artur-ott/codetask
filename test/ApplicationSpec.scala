import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.Services.userService
import org.apache.commons.codec.binary.Base64

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  def encodeBase64(s: String) = new String(Base64.encodeBase64(s.getBytes))
  val auth = "Basic " + encodeBase64("admin@a.pp:$1amn_$2pwrt")

  val user1 = new User(User.NEW, "admin@test.de", "teacher", userService.passwordHash("test"), List())
  userService.create(user1)

  val user2 = new User(User.NEW, "test@test.de", "student", userService.passwordHash("test"), List())
  userService.create(user2)

  val loop = 6000

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)
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

    "create Course" in new WithApplication {
      val json: JsValue = Json.parse("""{"id": 0, "title": "Neuer Kurs 5","chapters": [{"id": 1, "title": "About Mutable Sets", "tasks": [{"id":"koan1","tag":"koan-task","data":{"description":"Mutable sets can be created easily","code":"val mySet = mutable.Set(\"Michigan\", \"Ohio\", \"Wisconsin\", \"Iowa\")\nmySet.size should be(__)\nmySet += \"Oregon\"\nmySet contains \"Oregon\" should be(__)","mode":"scala","solutions":["4","true"]},"solution":"4,true"},{"id":"koan2","tag":"koan-task","data":{"description":"Mutable sets can have elements removed","code":"val mySet = mutable.Set(\"Michigan\", \"Ohio\", \"Wisconsin\", \"Iowa\")\nmySet -= \"Ohio\"\nmySet contains \"Ohio\" should be(__)","mode":"scala","solutions":["false"]},"solution":"false"},{"id":"koan3","tag":"koan-task","data":{"description":"Mutable sets can have tuples of elements removed","code":"val mySet = mutable.Set(\"Michigan\", \"Ohio\", \"Wisconsin\", \"Iowa\")\nmySet -= (\"Iowa\", \"Ohio\")\nmySet contains \"Ohio\" should be(__)\nmySet.size should be(__)","mode":"scala","solutions":["false","2"]},"solution":"false,2"},{"id":"koan4","tag":"koan-task","data":{"description":"Mutable sets can have tuples of elements added","code":"val mySet = mutable.Set(\"Michigan\", \"Wisconsin\")\nmySet += (\"Iowa\", \"Ohio\")\nmySet contains \"Ohio\" should be(__)\nmySet.size should be(__)","mode":"scala","solutions":["true","4"]},"solution":"true,4"},{"id":"koan5","tag":"koan-task","data":{"description":"Mutable sets can have Lists of elements added","code":"val mySet = mutable.Set(\"Michigan\", \"Wisconsin\")\nmySet ++= List(\"Iowa\", \"Ohio\")\nmySet contains \"Ohio\" should be(__)\nmySet.size should be(__)","mode":"scala","solutions":["true","4"]},"solution":"true,4"},{"id":"koan6","tag":"koan-task","data":{"description":"Mutable sets can have Lists of elements removed","code":"val mySet = mutable.Set(\"Michigan\", \"Ohio\", \"Wisconsin\", \"Iowa\")\nmySet --= List(\"Iowa\", \"Ohio\")\nmySet contains \"Ohio\" should be(__)\nmySet.size should be(__)","mode":"scala","solutions":["false","2"]},"solution":"false,2"},{"id":"koan7","tag":"koan-task","data":{"description":"Mutable sets can be cleared","code":"val mySet = mutable.Set(\"Michigan\", \"Ohio\", \"Wisconsin\", \"Iowa\")\nmySet.clear() // Convention is to use parens if possible when method called changes state\nmySet contains \"Ohio\" should be(__)\nmySet.size should be(__)","mode":"scala","solutions":["false","0"]},"solution":"false,0"}]}]}""")
      val request = FakeRequest(POST, "/api/courses")
          .withJsonBody(json)
          .withHeaders(("Authorization", auth))
        val result = route(request)

      status(result.get) must equalTo(CREATED)
      header("Location", result.get).get must contain("/api/courses/0")
    }

    "subscribe user to course" in new WithApplication {
      val request = FakeRequest(GET, "/subscribe/0")
        .withSession("username" -> "test@test.de", "password" -> "test")

      val result = route(request)
      status(result.get) must equalTo(SEE_OTHER)
      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
    }

    "store state of chapter" in new WithApplication {
        val json: JsValue = Json.parse(
          """{"courseId":0,"chapterId":1,"taskSolutions":[{"taskId":"koan1","taskState":{"mySolutions":["4","true"]}},{"taskId":"koan2","taskState":{"mySolutions":["false"]}},{"taskId":"koan3","taskState":{"mySolutions":["false","2"]}},{"taskId":"koan4","taskState":{"mySolutions":[]}},{"taskId":"koan5","taskState":{"mySolutions":[]}},{"taskId":"koan6","taskState":{"mySolutions":[]}},{"taskId":"koan7","taskState":{"mySolutions":[]}}]}"""
        )

        val request = FakeRequest(POST, "/api/solutions/0")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)


        contentAsString(result.get) must contain("\"status\":\"OK\"")
        status(result.get) must equalTo(OK)
    }

    "get Course" in new WithApplication {
      val request = FakeRequest(GET, "/api/courses/0")
        val result = route(request)

      var failed = 0
      for(_ <- 1 to loop) {
        val r = route(request)
        if (status(r.get) != OK) failed = failed + 1
      }

      status(result.get) must equalTo(OK)
      failed shouldEqual(0)
    }

    "get all Students" in new WithApplication {
      val request = FakeRequest(GET, "/api/users/students")
         .withSession("username" -> "admin@test.de", "password" -> "test")
      val result = route(request)

      var failed = 0
      for(_ <- 1 to loop) {
        val r = route(request)
        if (status(r.get) != OK) failed = failed + 1
      }

      status(result.get) must equalTo(OK)
      failed shouldEqual(0)
    }

    "retreive courses" in new WithApplication {
        val request = FakeRequest(GET, "/api/courses/all")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        var failed = 0
        for(_ <- 1 to loop) {
          val r = route(request)
          if (status(r.get) != OK) failed = failed + 1
        }

        status(result.get) must equalTo(OK)
        failed shouldEqual(0)
        contentAsString(result.get) must contain("{\"id\":0,\"title\":\"Neuer Kurs 5\"")
    }

    "fail to store malformated state of chapter" in new WithApplication {
        val json: JsValue = Json.parse(
          """{
            "courseId": 0,
            "chapterId": 1,
            "taskStates": [
              {
                "taskId": "koan1"
              }
            ]
          }"""
        )

        val request = FakeRequest(POST, "/api/solutions/0")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "retreive chapter states" in new WithApplication {
        val request = FakeRequest(GET, "/api/solutions/0")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(OK)
        contentAsString(result.get) must contain("\"mySolutions\":[\"4\",\"true\"]")
    }

//    "unsubscribe user from course" in new WithApplication {
//      val request = FakeRequest(GET, "/unsubscribe/0")
//        .withSession("username" -> "test@test.de", "password" -> "test")
//
//      val result = route(request)
//      status(result.get) must equalTo(SEE_OTHER)
//      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
//    }
//
//    "delete course" in new WithApplication {
//        val request = FakeRequest(DELETE, "/api/courses/0")
//          .withSession("username" -> "admin@test.de", "password" -> "test")
//        val result = route(request)
//
//        status(result.get) must equalTo(OK)
//        contentAsString(result.get) must contain("{\"status\":\"OK\"")
//    }
  }
}