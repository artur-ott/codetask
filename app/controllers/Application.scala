package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import models.CodeTask

class Application extends Controller with Secured {

	def index = Action {
		Ok(views.html.index("Your new application is ready."))
	}

	def test = withUser { username => implicit request =>
		Ok(views.html.polytest())
	}

	def testInterpret(code: String) = Action {
		Ok(new CodeTask("", code, "").run().consoleOutput)
	}

	def socket = WebSocket.acceptWithActor[String, String] { request => out =>
		MyWebSocketActor.props(out)
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
