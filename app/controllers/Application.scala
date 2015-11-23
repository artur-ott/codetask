package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.CodeTask

class Application extends Controller {

	def index = Action {
		Ok(views.html.index("Your new application is ready."))
	}

	def test = Action {
		Ok(views.html.test())
	}

	def testInterpret(code: String) = Action {
		Ok(new CodeTask("", code, "").run().consoleOutput)
	}
}
