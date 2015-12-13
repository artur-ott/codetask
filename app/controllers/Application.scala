package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import scala.collection.JavaConverters._
import models._

class Application extends Controller with Secured {

    // tests
    def testInterpret(code: String) = Action {
        Ok(new CodeTask("", code, "").run().consoleOutput)
    }

    def test() = Action {
        Ok("Your Application is ready.")
    }


    // /tests

    def index() = Action {
        Redirect(routes.Auth.login)
    }

    def solutionSave(courseName: String, chapterName:String, taskName: String) = withUser(parse.json) { 
        user => implicit request =>

        var result = Json.obj("success" -> "state saved")
        val jsObj = request.body.asInstanceOf[JsObject]

        if (jsObj.keys.contains("state")) {
            try {
                val chapter = user.courses(courseName)(chapterName) + (taskName -> jsObj)
                val course = user.courses(courseName) + (chapterName -> chapter)
                user.courses = user.courses + (courseName -> course)
    
                Services.userService.update(user)
            } catch {
                case e: Exception => 
                    result = Json.obj("error" -> "false course/task")
            }
        } else {
            result = Json.obj("error" -> "false obj format")
        }

        Ok(result)
    }

    def solutionsJSON(courseName: String) = withUser { user => implicit request =>
        //try {
            if (!user.courses(courseName).isEmpty) {
                val chapters = user.courses(courseName).map { chapter =>
                    println(chapter._2)
                    JsObject(Seq(
                        "title" -> JsString(chapter._1),
                        "tasks" -> JsObject(chapter._2)
                    ))
                }
                 val result = JsObject(Seq(
                    "course" -> JsObject(Seq(
                        "title" -> JsString("course1"),
                        "chapters" -> JsArray(chapters.toList)
                    ))
                ))
                Ok(result)
            } else {
                Ok(Json.obj("error" -> "no solution for course"))
            }
        //} catch {
           // case e: Exception => Ok(Json.obj("error" -> e.getMessage))
        //}
    }
    
    def courseSave(courseName: String) = withUser(parse.json) { user => implicit request =>
        //val exists = Services.courseService.findOneByName(courseName)
        val course = Services.courseService.create(new Course(courseName, request.body.asInstanceOf[JsObject]))
        Ok("test")
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
            "koan1": {"description": "Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.\n Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.\n Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen","code": "val l = List(1, 2, 3, 4)\n    val l2 = l.map { x => x + 1 }\n    val l3 = l.map { x => x * x }\n    \n    l should be (__)\n    l2 should be(__)\n    l3 shouldBe __","solutions": ["List(1, 2, 3, 4)","List(2, 3, 4, 5)","List(1, 4, 9, 16)"]},
            "koan2": {"description": "Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.","code": "val x = 1\nval y = 300\n//some\n//lonely\n//comment\n//to\n//add\n//lines\nval l = List(1, 3, 5)\nval l2 = l ++ List(6)\n    \nl2 shouldBe __","solutions": ["List(1, 3, 5, 6)"]},
            "codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben.\n Nutzen Sie nicht die bereits vorhandenen Möglichkeit\n <b>List.reverse</b>","code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"},
            "koan3": {"description": "Java Koan","code": "@RunWith(KoanRunner.class)\n  public class MyKoans {\n    @Koan\n    public void test() {\n      int i= 10;\n      int j = 5;\n      int product = i * j;\n\n      assertThat(product, is(__)\n    }\n  }","solutions": ["50"]}
        }
    }]
    }
}"""))
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