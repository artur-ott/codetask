package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import models.Services._
import models.User
import models.User._
import models.CourseParser

class Application extends Controller with Secured {

  // Links

  def index() = Action { request =>
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
    var info: List[(Long, String, Int)] = List()
    user.subscriptions.foreach { courseId =>
      val solutions = user.chapterSolutions.filter(_.courseId == courseId)
      courseService.findOneById(courseId) match {
        case Some(c) => info = (c.id, c.title, progressOf(c, solutions)) :: info
        case None =>
      }
    }
    if (user.authority == "teacher" || user.authority == "admin") {
      val courses = courseService.findAll()
      Ok(views.html.teacherTryCourses(info))
    } else {
      Ok(views.html.dashboard(info))
    }
  }

  def overview() = withUser { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      val courses = courseService.findAll()
      Ok(views.html.teacherDashboard2(courses))
    } else {
      Unauthorized("not authorized")
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
      if (user.authority == "teacher" || user.authority == "admin") {
        Ok(views.html.teacherTryCourse(course.get.id, course.get.title))
      } else {
        Ok(views.html.course(course.get.id, course.get.title))
      }
    else
      Redirect(routes.Application.dashboard)
  }

  def users() = withUser { user => implicit request => 
    if (user.authority == "teacher" || user.authority == "admin") {
      val list = userService.findAll().map { u =>
        (u.id, u.username, u.authority)
      }
      Ok(views.html.teacherUsers(list))
    } else {
      Unauthorized("not authorized")
    }
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

  def courseStats(courseId: Long) = withUser { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      courseService.findOneById(courseId) match {
        case Some(course) =>
          val users = userService.findAll()
          val courseUsers = users.filter { user =>
            user.subscriptions.contains(courseId)
          }
          Ok(views.html.teacherCourseStats(courseUsers, course))
        case None => NotFound("course does not exists")
      }
    } else {
      Unauthorized("not authorized")
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
          // delete solutions of course from users
          userService.findAll().foreach { u =>
            if (u.subscriptions.contains(courseId)) {
              u.subscriptions = u.subscriptions.filter(_ != courseId)
              u.chapterSolutions = u.chapterSolutions.filter(_.courseId != courseId)
              userService.update(u)
            }
          }
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

  def deleteUser(userId: Long) = withUser { user => implicit request =>
    if (user.authority != "admin") {
      BadRequest("no authority")
    } else {
      userService.findOneById(userId) match {
        case Some(user) =>
          userService.delete(user)
          Redirect(routes.Application.users)
        case None => NotFound("user does not exists")
      }
    }
  }

  def unsubscribeUserFromCourse(userId: Long, courseId: Long) = withUser { user => implicit request =>
    if (user.authority != "admin" && user.authority != "teacher") {
      BadRequest("no authority")
    } else {
      userService.findOneById(userId) match {
        case Some(user) =>
          user.subscriptions = user.subscriptions.filter(_ != courseId)
          userService.update(user) match {
            case Some(u) => Redirect(routes.Application.courseStats(courseId))
            case None => BadRequest("could not update user")
          }
        case None => NotFound("user does not exists")
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
                "success" -> Messages("form.github.courseadded")
              )
              case None => Redirect(routes.Application.githubCourseForm).flashing(
                "failure" -> Messages("form.github.alreadyexists")
              )
            }
          } catch {
            case e: Exception => Redirect(routes.Application.githubCourseForm).flashing(
              "failure" -> Messages("form.github.invalidinfo")
            )
          }
        }
      )
    } else {
      Unauthorized("not authorized")
    }
  }


  val addFileCourseForm = Form(
    single(
      "Course Title" -> nonEmptyText
    )
  )

  def fileCourseForm() = Action { implicit request =>
    Ok(views.html.fileCourseForm(addFileCourseForm))
  }

  def postFileCourseForm = withUser(parse.multipartFormData) { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      addFileCourseForm.bindFromRequest.fold(
        formWithErrors => {
          Redirect(routes.Application.fileCourseForm).flashing(
            "failure" -> Messages("form.folder.notitle"))
        },
        title => {
          val files = request.body.files.toArray.filter(_.contentType != "text/x-scala").map(_.ref.file)

          if (files.size > 0) {
            try {
              val course = CourseParser.parseFromFiles(files, title)

              courseService.create(course) match {
                case Some(c) => Redirect(routes.Application.fileCourseForm).flashing(
                  "success" -> Messages("form.github.courseadded"))
                case None => Redirect(routes.Application.fileCourseForm).flashing(
                  "failure" -> Messages("form.folder.alreadyexists"))
              }
            } catch {
              case e: Exception => Redirect(routes.Application.fileCourseForm).flashing(
                "failure" -> Messages("form.folder.error"))
            }
          } else {
            Redirect(routes.Application.fileCourseForm).flashing(
              "failure" -> Messages("form.folder.uploadfiles"))
          }
        }
      )
    } else {
      Unauthorized("not authorized")
    }
  }


  val userForm = Form(
      tuple(
        "Username" -> nonEmptyText,
        "Authority" -> nonEmptyText,
        "Password" -> nonEmptyText,
        "Password" -> nonEmptyText
      )
  )

  def createUserForm() = Action { implicit request =>
    Ok(views.html.createUserForm(userForm))
  }

  def postCreateUserForm = withUser { user => implicit request =>
    if (user.authority == "admin") {
      userForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.createUserForm(formWithErrors))
        },
        form => {
          val user = User(-1, form._1, form._2, form._3)
          userService.create(user) match {
            case Some(u) => Redirect(routes.Application.createUserForm).flashing(
              "success" -> Messages("form.users.created"))
            case None => Redirect(routes.Application.createUserForm).flashing(
              "failure" -> Messages("form.users.exists"))
          }
        }
      )
    } else {
      Unauthorized("not authorized")
    }
  }

  val addUpdateUserForm = Form(
    tuple(
      "Username" -> text,
      "Authority" -> text,
      "Password" -> text,
      "Password" -> text
    ) verifying ("register.nomatch", result => result match {
      case (email, auth, password, password2) => password == password2
    })
  )

  def updateUserForm(userId: Long) = Action { implicit request =>
    Ok(views.html.updateUserForm(addUpdateUserForm, userId))
  }

  def postUpdateUserForm(userId: Long) = withUser { user => implicit request =>
    if (user.authority == "admin") {
      addUpdateUserForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.updateUserForm(formWithErrors, userId))
        },
        form => {
          userService.findOneById(userId) match {
            case Some(user) =>
              if (form._1 != "") {
               // user.username = form._1
              }
              if (form._2 != "") {
                user.authority = form._2
              }
              if (form._3 != "" && form._4 == form._3) {
                user.password = userService.passwordHash(form._3)
              }
              userService.update(user) match {
                case Some(u) => Redirect(routes.Application.updateUserForm(userId)).flashing(
                  "success" -> Messages("form.users.updated"))
                case None => Redirect(routes.Application.updateUserForm(userId)).flashing(
                  "failure" -> Messages("form.users.existsnot"))
              }
            case None => Redirect(routes.Application.updateUserForm(userId)).flashing(
              "failure" -> Messages("form.users.existsnot"))
          }
        }
      )
    } else {
      Unauthorized("not authorized")
    }
  }


 /* val userForm = Form(
      tuple(
        "Username" -> nonEmptyText,
        "Authority" -> nonEmptyText,
        "Password" -> nonEmptyText,
        "Password" -> nonEmptyText
      )
  )
*/

  def updateUploadCourse(courseId: Long) = withUser { user => implicit request =>
    if (user.authority != "teacher" && user.authority != "admin") {
      BadRequest("no authority")
    } else {
      Ok(views.html.updateFileCourseForm(courseId))
    }
  }

  def postUpdateFileCourseForm(courseId: Long) = withUser(parse.multipartFormData) { user => implicit request =>
    if (user.authority == "teacher" || user.authority == "admin") {
      val files = request.body.files.toArray.filter(_.contentType != "text/x-scala").map(_.ref.file)

      if (files.size > 0) {
        try {
          courseService.findOneById(courseId) match {
            case Some(c) => 
              val course = CourseParser.parseFromFiles(files, c.title)
              course.id = courseId

              courseService.update(course) match {
                case Some(c) => Redirect(routes.Application.updateUploadCourse(courseId)).flashing(
                  "success" -> Messages("form.github.courseadded"))
                case None => Redirect(routes.Application.updateUploadCourse(courseId)).flashing(
                  "failure" -> Messages("form.folder.alreadyexists"))
              }
            case None =>
              Redirect(routes.Application.updateUploadCourse(courseId)).flashing(
                "failure" -> Messages("form.courseupdate.existsnot")
              )
          }
        } catch {
          case e: Exception => Redirect(routes.Application.updateUploadCourse(courseId)).flashing(
            "failure" -> Messages("form.folder.error"))
        }
      } else {
        Redirect(routes.Application.updateUploadCourse(courseId)).flashing(
          "failure" -> Messages("form.folder.uploadfiles"))
      }
    } else {
      Unauthorized("not authorized")
    }
  }
}
