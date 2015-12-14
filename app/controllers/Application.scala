package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import scala.collection.JavaConverters._
import models._

class Application extends Controller with Secured {

    def index() = Action {
        Redirect(routes.Auth.login)
    }

    def dashboard() = withUser { user => implicit request =>
        val courses = user.courses.map { course => 
            val c = Services.courseService.findOneByName(course._1)
            (c.get.id, course._1, 0) 
        }
        Ok(views.html.dashboard(courses.toList))
    }

    def course(courseId: Long) = withUser { user => implicit request =>
        val course = Services.courseService.findOneById(courseId)
        val courseName = course.get.name
        if (user.courses.contains(courseName) && (course.isEmpty != true)) {
            Ok(views.html.course(courseId, courseName))
        } else {
            Redirect(routes.Application.dashboard)
        }
    }

    def subscribe(courseId: Long) = withUser { user => implicit request =>
        val course = Services.courseService.findOneById(courseId)
        val courseName = course.get.name
        if (course != None && 
            user.courses.find { course => course._1 == courseName } == None) {
            user.courses += (courseName -> Map())
            Services.userService.update(user)
        }
        Redirect(routes.Application.dashboard)
    }

    def unsubscribe(courseId: Long) = withUser { user => implicit request =>
        val course = Services.courseService.findOneById(courseId)
        val courseName = course.get.name
        user.courses -= courseName
        Services.userService.update(user)
        Redirect(routes.Application.dashboard)
    }

    def coursesJson() = Action {
        Ok(Json.toJson(Services.courseService.findAll.map{
            course => JsObject(Map(
                "id" -> JsNumber(course.id), 
                "title" -> JsString(course.name))
            )
        }.toSeq))
    }

    def courseJson(courseId: Long) = withUser { user => implicit request =>
        Services.courseService.findOneById(courseId) match {
            case Some(course) => Ok(course.json)
            case None         => Ok(Json.obj("error" -> "course not found"))
        }
    }

    def storeCourseJson() = withUser(parse.json) { user => implicit request =>
        try {
            val id = Services.courseService.getId()
            val json = request.body.toString
            val name = request.body.as[JsObject]
                .value("course").as[JsObject]
                .value("title").as[String]
            val course = new Course(id, name, json)

            Services.courseService.create(course) match {
                case None => Services.courseService.update(course) match {
                    case None => println("ex"); throw new Exception
                    case Some(x) => println("course updated")
                }
                case Some(x) => println("course saved")
            }
            Ok(Json.obj("success" -> "course saved"))
        } catch {
            case e: Exception => Ok(Json.obj("error" -> "json error"))
        }
    }

    def storeSolutionJson(courseId: Long) = withUser(parse.json) { 
        user => implicit request =>

        val course = Services.courseService.findOneById(courseId)
        val courseName = course.get.name
        var json = request.body.toString
        var result = Json.obj("success" -> "state saved")
        var chapterName = request.body.as[JsObject].value("title").as[String]
        try {
            val course = user.courses(courseName) + (chapterName -> json)
            user.courses = user.courses + (courseName -> course)

            Services.userService.update(user)
        } catch {
            case e: Exception => 
                result = Json.obj("error" -> "false course/chapter")
        }


        Ok(result)
    }

    def solutionsJson(courseId: Long) = withUser { user => implicit request =>
        val course = Services.courseService.findOneById(courseId)
        val courseName = course.get.name
        try {
            if (!user.courses(courseName).isEmpty) {
                val chapters = user.courses(courseName).map { chapter =>
                    JsObject(Seq(
                        "title" -> JsString(chapter._1),
                        "tasks" -> Json.parse(chapter._2)
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
        } catch {
            case e: Exception => Ok(Json.obj("error" -> e.getMessage))
        }
    }
    
    def deleteCourse(courseId: Long) = withUser { user => implicit request =>
        Services.courseService.delete(new Course(courseId, "", "")) match {
            case Some(x) => Ok(Json.obj("success" -> "course deleted"))
            case None => Ok(Json.obj("error" -> "could not delete course"))
        }
    }

    // tests
    def testInterpret() = withUser(parse.json) { user => implicit request =>
        var code = request.body.as[JsObject].value("code").as[String]
        println(code)
        Ok(Json.obj("output" -> new CodeTask("", code, "").run().consoleOutput))
    }

    def test() = Action {
        Ok("Your Application is ready.")
    }
    // /tests
}