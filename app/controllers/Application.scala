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
	def testInterpret(code: String) = Action {
		Ok(new CodeTask("", code, "").run().consoleOutput)
	}

	def test() = Action {
		Ok("Your Application is ready.")
	}

	def solution = Action(BodyParsers.parse.json) { request =>
		println(request.body.toString)
		Ok(Json.obj("success" -> "done"))
		// TODO
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
        "title": "About Scala Lists",
        "tasks": {
            "video1": {"description": "In diesem Kapitel sollend Listen in Scala näher erläutert werden\n Listen sind collections und können objekte speichern\n Listen sind prinzipiell immutable also unveränderbar\n Im folgenden Video werden Listen ausfürlich erläutert","url": "U23j6yH21W4"},
            "koan1": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\n    val l2 = l.map { x => x + 1 }\n    val l3 = l.map { x => x * x }\n    \n    l should be (__)\n    l2 should be(__)\n    l3 shouldBe __","solutions": "List(1, 2, 3, 4);List(2, 3, 4, 5);List(1, 4, 9, 16)"},
            "koan2": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val l = List(1, 3, 5)\n    val l2 = l ++ List(6)\n    \n    l2 shouldBe __","solutions": "List(1, 3, 5, 6)"},
            "codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>","code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"}
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