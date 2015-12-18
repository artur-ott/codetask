package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import scala.collection.JavaConverters._
import models._
import Services._
import models.Course._

class Application extends Controller with Secured {

  def index() = Action {
    Redirect(routes.Auth.login)
  }

  def dashboard() = withUser { user => implicit request =>
    var info: List[(Long, String, Int)] = List()

    user.chapterStates.foreach { chapterState =>
      courseService.findOneById(chapterState.courseId) match {
        case Some(c) => info = (c.id, c.title, progressOf(c)) :: info
        case None =>
      }
    }
    Ok(views.html.dashboard(info))
  }
//
//  def course(courseId: Long) = withUser { user => implicit request =>
//    val course = Services.courseService.findOneById(courseId)
//    val courseName = course.get.name
//    if (user.courses.contains(courseName) && (course.isEmpty != true)) {
//      Ok(views.html.course(courseId, courseName))
//    } else {
//      Redirect(routes.Application.dashboard)
//    }
//  }
//
//  def subscribe(courseId: Long) = withUser { user => implicit request =>
//    val course = Services.courseService.findOneById(courseId)
//    val courseName = course.get.name
//    if (course != None && 
//      user.courses.find { course => course._1 == courseName } == None) {
//      user.courses += (courseName -> Map())
//      Services.userService.update(user)
//    }
//    Redirect(routes.Application.dashboard)
//  }
//
//  def unsubscribe(courseId: Long) = withUser { user => implicit request =>
//    val course = Services.courseService.findOneById(courseId)
//    val courseName = course.get.name
//    user.courses -= courseName
//    Services.userService.update(user)
//    Redirect(routes.Application.dashboard)
//  }
//
  def coursesJson() = Action {
    val list = Services.courseService.findAll.map {
        course => JsObject(Map(
          "id" -> JsNumber(course.id), 
          "title" -> JsString(course.title)
        ))
    }.toSeq
    Ok(Json.toJson(list))
  }
//
//  def courseJson(courseId: Long) = withUser { user => implicit request =>
//    Services.courseService.findOneById(courseId) match {
//      case Some(course) => Ok(course.json)
//      case None     => Ok(Json.obj("error" -> "course not found"))
//    }
//  }

  def saveCourseJson() = withUser(parse.json) { user => implicit request =>
    val courseResult = request.body.validate[Course]
    courseResult.fold(
      errors => {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      course => {
        courseService.create(course) match {
          case Some(saved) =>  Ok(Json.obj(
            "status" -> "OK", 
            "message" -> ("Course '" + saved.title + "' saved")
          ))
          case None =>  BadRequest(Json.obj(
            "status" -> "KO", 
            "message" -> ("Course '" + course.title + "' already exists")
          ))
        }
      }
    )
  }

//  def storeSolutionJson(courseId: Long) = withUser(parse.json) { 
//    user => implicit request =>
//
//    val course = Services.courseService.findOneById(courseId)
//    val courseName = course.get.name
//    var json = request.body.toString
//    var result = Json.obj("success" -> "state saved")
//    var chapterName = request.body.as[JsObject].value("title").as[String]
//    try {
//      val course = user.courses(courseName) + (chapterName -> json)
//      user.courses = user.courses + (courseName -> course)
//
//      Services.userService.update(user)
//    } catch {
//      case e: Exception => 
//        result = Json.obj("error" -> "false course/chapter")
//    }
//
//
//    Ok(result)
//  }
//
//  def solutionsJson(courseId: Long) = withUser { user => implicit request =>
//    val course = Services.courseService.findOneById(courseId)
//    val courseName = course.get.name
//    try {
//      if (!user.courses(courseName).isEmpty) {
//        val chapters = user.courses(courseName).map { chapter =>
//          JsObject(Seq(
//            "title" -> JsString(chapter._1),
//            "tasks" -> Json.parse(chapter._2)
//          ))
//        }
//         val result = JsObject(Seq(
//          "course" -> JsObject(Seq(
//            "title" -> JsString("course1"),
//            "chapters" -> JsArray(chapters.toList)
//          ))
//        ))
//        Ok(result)
//      } else {
//        Ok(Json.obj("error" -> "no solution for course"))
//      }
//    } catch {
//      case e: Exception => Ok(Json.obj("error" -> e.getMessage))
//    }
//  }
//  
//  def deleteCourse(courseId: Long) = withUser { user => implicit request =>
//    Services.courseService.delete(new Course(courseId, "", "")) match {
//      case Some(x) => Ok(Json.obj("success" -> "course deleted"))
//      case None => Ok(Json.obj("error" -> "could not delete course"))
//    }
//  }
//
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