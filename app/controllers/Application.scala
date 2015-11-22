package controllers

import play.api._
import play.api.mvc._
import models.CodeTask

class Application extends Controller {

	def index = Action {
		Ok(views.html.index("Your new application is ready."))
	}


	// test
	val ct = new CodeTask(
		"Write a function 'reverse' that returns a reversed list",
		"def reverse(l: List[Any]): List[Any] = {\n  //code hier \n}",
		"assert(reverse(List(1,2,3)) == List(3,2,1), \"liste ungleich\"")

	def test = Action {
		Ok(views.html.test(ct))
	}
}
