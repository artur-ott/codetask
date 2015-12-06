package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import models.{CodeTask, User}
import play.api.libs.json._
import scala.collection.JavaConverters._

class Application extends Controller with Secured {

	// tests

	def secureTest() = withUser { user => implicit request =>
		Ok(views.html.polytest())
	}

	def testInterpret(code: String) = Action {
		Ok(new CodeTask("", code, "").run().consoleOutput)
	}

	def socket() = WebSocket.acceptWithActor[String, String] { request => out =>
		MyWebSocketActor.props(out)
	}

	def test() = Action {
		Ok("Your Application is ready.")
	}

	// /tests

	def index() = Action {
		Redirect(routes.Auth.login)
	}

	def coursesJSON() = Action {
		Ok(Json.toJson(Services.courseService.findAll.map{course => course.name}.toSeq))
	}

	def courseJSON(courseName: String) = withUser { user => implicit request =>
		Ok(Json.parse("""{ "course": {
		"title": "scala1",
		"chapters": [{
			"title": "AboutTest",
			"tasks": {
				"video1": {"description": "description","url": "https://www.youtube.com/watch?v=Y7VLcx4fz4A"},
				"koan1": {"description": "das ist ein koan eine aufgabe mit fehlenden assert werten","code": "result should equal (__)\nresult should === (__)\nresult should be __\nresult shouldEqual __\nresult shouldBe __","solutions": "3;3;List(3, 2, 1);\\\"text\\\";3"},
				"codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben","code": "def rvrs(l: List[Any]): List[Any] = {\n//solve\n}\n\n","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"}
			}
		},{
			"title": "AboutVal",
			"tasks": {
				"koan1": {"description": "test", "code": "1 should be __", "solution": "1"}
			}
		}]
	}
}"""))
	}

	def solutionsJSON(courseName: String) = Action {//withUser { user => implicit request =>
		//val solutions = user.courses get courseName
		//if (solutions == None) {
			//Ok(Json.toJson("""{"error": "empty"}"""))
		//} else {
			//val json = Json.toJson(solutions.get.map { task => Json.toJson(task._2) })
			//Ok(json)
		//}
		Ok(Json.toJson("""[{"koan1": "3;3"}]"""))
	}

	def course(courseName: String) = withUser { user => implicit request =>
		val course = Services.courseService.findOneByName(courseName)
		if (user.courses.contains(courseName) && (course.isEmpty != true)) {
			Ok(views.html.course(courseName))
		} else {
			Redirect(routes.Application.dashboard)
		}
	}

	def courseSubscribe(courseName: String) = withUser { user => implicit request =>
		if (user.courses.find {course => course._1 == courseName}.isEmpty && 
		   (Services.courseService.findOneByName(courseName).isEmpty != true)) {
			user.courses += (courseName -> Map())
			Services.userService.update(user)
		}
		Redirect(routes.Application.dashboard)
	}

	def courseUnsubscribe(courseName: String) = withUser { user => implicit request =>
		user.courses -= courseName
		Services.userService.update(user)
		Redirect(routes.Application.dashboard)
	}

	def dashboard() = withUser { user => implicit request =>
		val courses = user.courses.map { course => (course._1, 1) }
		Ok(views.html.dashboard(courses.toList))
	}
}

import akka.actor._

object MyWebSocketActor {
	def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
	def receive = {
		case msg: String => {
			println(msg)
			val s = new CodeTask(code = msg).run().consoleOutput
			println(s)
			out ! (new CodeTask(code = msg).run().consoleOutput)
		}
	}
}
