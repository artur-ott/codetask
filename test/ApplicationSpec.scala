import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.Services.userService

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  val id1 = userService.newId()
  val user1 = new User(id1, "admin@test.de", "teacher", userService.passwordHash("test"), List())
  userService.create(user1)

  val id2 = userService.newId()
  val user2 = new User(id2, "test@test.de", "student", userService.passwordHash("test"), List())
  userService.create(user2)

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

//    "subscribe user to course" in new WithApplication {
//      val request = FakeRequest(GET, "/subscribe/100001")
//        .withSession("username" -> "test@test.de", "password" -> "test")
//
//      val result = route(request)
//      status(result.get) must equalTo(SEE_OTHER)
//      redirectLocation(result.get) must beSome.which(_ == "/dashboard")
//    }


    "retreive courses json" in new WithApplication {
        val request = FakeRequest(GET, "/api/courses")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(OK)
        contentAsString(result.get) must contain("{\"id\":100001,\"title\":\"Scala For Beginners\"")
    }

    "retreive course json" in new WithApplication {
        val request = FakeRequest(GET, "/api/courses/100001")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(OK)
        contentAsString(result.get) must contain("\"title\":\"Scala For Beginners\"")
    }

    "store state of chapter" in new WithApplication {
        val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskSolutions": [
              {
                "taskId": "video1",
                "taskState": {"status": "watched"}
              },{
                "taskId": "koan1",
                "taskState": {"mySolutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]}
              },{
                "taskId": "code1",
                "taskState": {"code": "test"}
              }
            ]
          }"""
        )

        val request = FakeRequest(POST, "/api/solutions/100001")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)


        contentAsString(result.get) must contain("\"status\":\"OK\"")
        status(result.get) must equalTo(OK)
    }

    "fail to store malformated state of chapter" in new WithApplication {
        val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskStates": [
              {
                "taskId": "koan1"
              }
            ]
          }"""
        )

        val request = FakeRequest(POST, "/api/solutions/100001")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "retreive chapter states" in new WithApplication {
        val request = FakeRequest(GET, "/api/solutions/100001")
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        status(result.get) must equalTo(OK)
        contentAsString(result.get) must contain("\"mySolutions\":[\"List(1, 2, 3, 4)\"")
    }

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
//        val request = FakeRequest(DELETE, "/api/courses/100001")
//          .withSession("username" -> "admin@test.de", "password" -> "test")
//        val result = route(request)
//
//        status(result.get) must equalTo(OK)
//        contentAsString(result.get) must contain("{\"status\":\"OK\"")
//    }

    "interpret scala code" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n  l.reverse\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"OK\"")
        contentAsString(result.get) must contain("\"message\":\"Code executed successfull.\"")
        status(result.get) must equalTo(OK)
    }

    "fail with error" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n  l\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        //contentAsString(result.get) must contain("\"message\":\"\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with infinite loop" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n  while(true) {}\nl\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"No completion check your code for infinite loops.\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with infinite recursion" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n rvrs(l)\n l\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("java.lang.StackOverflowError")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with System.exit()" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nSystem.exit(1)"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"Your code uses prohibited code in line: 4\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with invalid import scala._" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "import scala._\ndef rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"Your code uses prohibited code in line: 1\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with invalid import tools._" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "import tools._\ndef rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"Your code uses prohibited code in line: 1\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

    "fail with invalid use of library tools" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nval x = new tools.nsc.interpreter.IMain()"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"Your code uses prohibited code in line: 4\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }

     "fail with ascii encoded use of library tools" in new WithApplication {
      val json: JsValue = Json.parse(
          """{
            "courseId": 100001,
            "chapterId": 1,
            "taskId": "code1",
            "code": "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nval x = new \164ools.nsc.interpreter.IMain()"
          }"""
        )

        val request = FakeRequest(POST, "/api/interpreter/scala")
          .withJsonBody(json)
          .withSession("username" -> "test@test.de", "password" -> "test")
        val result = route(request)

        contentAsString(result.get) must contain("\"status\":\"KO\"")
        contentAsString(result.get) must contain("\"message\":\"Your code uses prohibited code in line: 4\"")
        status(result.get) must equalTo(BAD_REQUEST)
    }
  }
}
