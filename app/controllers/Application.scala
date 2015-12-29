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
import models.User._

class Application extends Controller with Secured {

  def index() = Action {
    Redirect(routes.Auth.login)
  }

  def dashboard() = withUser { user => implicit request =>
    var info: List[(Long, String, Int)] = List()
    user.subscriptions.foreach { courseId =>
      val states = user.chapterStates.filter(_.courseId == courseId)
      courseService.findOneById(courseId) match {
        case Some(c) => info = (c.id, c.title, progressOf(c, states)) :: info
        case None =>
      }
    }
    Ok(views.html.dashboard(info))
  }

  def course(courseId: Long) = withUser { user => implicit request =>
    val course = courseService.findOneById(courseId)
    if (user.subscriptions.contains(courseId) && !course.isEmpty)
      Ok(views.html.course(course.get.id, course.get.title))
    else
      Redirect(routes.Application.dashboard)
  }

  def subscribe(courseId: Long) = withUser { user => implicit request =>
    courseService.findOneById(courseId) match {
      case Some(course) => 
        user.subscriptions = user.subscriptions + courseId
        userService.update(user)
      case None => 
    }
    Redirect(routes.Application.dashboard)
  }

  def unsubscribe(courseId: Long) = withUser { user => implicit request =>
    user.subscriptions = user.subscriptions.filter(_ != courseId)
    user.chapterStates = user.chapterStates.filter(_.courseId != courseId)
    userService.update(user)
    Redirect(routes.Application.dashboard)
  }

  def coursesJson() = Action {
    val list = Services.courseService.findAll.map {
        course => JsObject(Map(
          "id" -> JsNumber(course.id), 
          "title" -> JsString(course.title)
        ))
    }.toSeq
    Ok(Json.toJson(list))
  }

  def courseJson(courseId: Long) = withUser { user => implicit request =>
    Services.courseService.findOneById(courseId) match {
      case Some(course) => Ok(Json.toJson(course))
      case None => Ok(Json.obj("error" -> "course not found"))
    }
  }

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

  def updateCourseJson(courseId: Long) = withUser(parse.json) { 
    user => implicit request =>

    val courseResult = request.body.validate[Course]
    courseResult.fold(
      errors => {
        BadRequest(Json.obj(
          "status" -> "KO",
          "message" -> JsError.toJson(errors)
        ))
      },
      course => {
        course.id = courseId
        courseService.update(course) match {
          case Some(saved) =>  Ok(Json.obj(
            "status" -> "OK", 
            "message" -> ("Course '" + saved.title + "' saved")
          ))
          case None =>  BadRequest(Json.obj(
            "status" -> "KO", 
            "message" -> ("Course id: '" + courseId + "' does not exists")
          ))
        }
      }
    )
  }

  def storeSolutionsJson(courseId: Long) = withUser(parse.json) { 
    user => implicit request =>

    val chapterStateResult = request.body.validate[ChapterState]
    chapterStateResult.fold(
      errors =>  {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      state => {
        println("chapterState now: " + user.chapterStates)
        user.chapterStates = state :: user.chapterStates.filter {
          x => x.courseId != state.courseId || x.chapterId != state.chapterId
        }
        println("\nchapterState new: " + user.chapterStates)
        userService.update(user)
        Ok(Json.obj("status" -> "OK", "message" -> ("User updated")))
      }
    )
  }

  def solutionsJson(courseId: Long) = withUser { user => implicit request =>
    val states = user.chapterStates.filter(_.courseId == courseId)
    Ok(Json.toJson(states))
  }
  
  def deleteCourse(courseId: Long) = withUser { user => implicit request =>
    courseService.findOneById(courseId) match {
      case Some(course) =>
        courseService.delete(course)
        Ok(Json.obj("success" -> "course deleted"))
      case None => Ok(Json.obj("error" -> "could not delete course"))
    }
  }

  // tests
  def testInterpret() = withUser(parse.json) { user => implicit request =>
    var code = request.body.as[JsObject].value("code").as[String]
    println(code)
    Ok(Json.obj("output" -> new CodeTask("", code, "").run().consoleOutput))
  }
  // /tests
}