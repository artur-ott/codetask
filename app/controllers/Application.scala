package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import models.{CodeTask, User}
import play.api.libs.json._
import scala.collection.JavaConverters._

class Application extends Controller with Secured {

	def index() = Action {
		Redirect(routes.Auth.login)
	}

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

	def courses() = Action {
		Ok(Json.toJson(Services.courseService.findAll.map{course => course.name}.toSeq))
	}

	def addCourse(course: String) = withUser { user => implicit request =>
		if (user.courses.find {c => c._1 == course}.isEmpty && 
		   (Services.courseService.findOneByName(course).isEmpty != true)) {
			user.courses += (course -> Map())
			Services.userService.update(user)
		}
		Redirect(routes.Application.dashboard)
	}

	def removeCourse(course: String) = withUser { user => implicit request =>
		user.courses -= course
		Services.userService.update(user)
		Redirect(routes.Application.dashboard)
	}

	def dashboard() = withUser { user => implicit request =>
		val courses = user.courses.map {course => (course._1, 1)}
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
