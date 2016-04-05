package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.CourseInfo
import models.Services._
import models.User._
import models.CourseParser

class Application extends Controller with Secured {

  // Links

  def index() = Action {
    Redirect(routes.Auth.login)
  }

  def menu() = withUser { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      Ok(views.html.teacherMenu())
    } else {
      Redirect(routes.Application.dashboard)
    }
  }

  def dashboard() = withUser { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      Ok(views.html.teacherDashboard())
    } else {
      var info: List[(Long, String, Int)] = List()
      user.subscriptions.foreach { courseId =>
        val solutions = user.chapterSolutions.filter(_.courseId == courseId)
        courseService.findOneById(courseId) match {
          case Some(c) => info = (c.id, c.title, progressOf(c, solutions)) :: info
          case None =>
        }
      }
      Ok(views.html.dashboard(info))
    }
  }
  
  def courses = withUser { user => implicit request => 
    if (user.authority == "teacher" || user.authority == "admin") {
      val list = courseService.findAll().map { c => 
        (c.id, c.title, c.githubUrl != None) 
      }
      Ok(views.html.teacherCourses(list))
    } else {
      Unauthorized("not authorized")
    }
  }

  def course(courseId: Long) = withUser { user => implicit request =>
    val course = courseService.findOneById(courseId)
    if (user.subscriptions.contains(courseId) && !course.isEmpty)
      Ok(views.html.course(course.get.id, course.get.title))
    else
      Redirect(routes.Application.dashboard)
  }

  def solution(courseId: Long, userId: Long) = withUser {
    user => implicit request =>

    if (user.authority != "teacher" && user.authority != "admin") {
      BadRequest("no authority")
    } else {
      courseService.findOneById(courseId) match {
        case Some(course) =>
          Ok(views.html.teacherCourse(courseId, userId, course.title))
        case None => NotFound("course does not exist")
      }
    }
  }

  // Operations
  
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
    user.chapterSolutions = user.chapterSolutions.filter(_.courseId != courseId)
    userService.update(user)
    Redirect(routes.Application.dashboard)
  }

  def deleteCourse(courseId: Long) = withUser { user => implicit request =>
    if (user.authority != "teacher" && user.authority != "admin") {
      BadRequest("no authority")
    } else {
      courseService.findOneById(courseId) match {
        case Some(course) =>
          courseService.delete(course)
          Redirect(routes.Application.courses)
        case None => NotFound("course does not exists")
      }
    }
  }

  def updateCourse(courseId: Long) = withUser { user => implicit request =>
    if (user.authority != "teacher" && user.authority != "admin") {
      BadRequest("no authority")
    } else {
      courseService.findOneById(courseId) match {
        case Some(course) =>
          course.githubUrl match {
            case Some(url) => 
              try {
                val c = CourseParser.parseFromGithub(url, course.title)
                c.id = course.id
                courseService.update(c)
              } catch {
                case e: Exception => play.Logger.error(e.toString)
              }
            case None =>
          }
          Redirect(routes.Application.courses)
        case None => NotFound("course does not exists")
      }
    }
  }

  // Forms

  val addCourseGithubForm = Form(
      tuple(
        "Course Title" -> nonEmptyText,
        "Github User" -> nonEmptyText,
        "Github Repo" -> nonEmptyText,
        "Repo Path" -> nonEmptyText
      )
  )

  def githubCourseForm() = Action { implicit request =>
    Ok(views.html.githubCourseForm(addCourseGithubForm))
  }
  
  def postGithubCourseForm() = withUser { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      addCourseGithubForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.githubCourseForm(formWithErrors)),
        form => {
          try {
            val url = CourseParser.mkGithubApiUrl(form._2, form._3, form._4)
            val course = CourseParser.parseFromGithub(url, form._1)
            courseService.create(course) match {
              case Some(c) => Redirect(routes.Application.githubCourseForm).flashing(
                "success" -> "Course added"
              )
              case None => Redirect(routes.Application.githubCourseForm).flashing(
                "failure" -> "course  already exists"
              )
            }
          } catch {
            case e: Exception => Redirect(routes.Application.githubCourseForm).flashing(
              "failure" -> "github info invalid"
            )
          }
        }
      )
    } else {
      Unauthorized("not authorized")
    }
  }

}