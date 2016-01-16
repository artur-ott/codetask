package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.JsValue._
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
    if (user.authority == "teacher") {
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

  def showCourse(courseId: Long, userId: Long) = withUser { 
    user => implicit request =>

    if (user.authority != "teacher") BadRequest("no authority")
    courseService.findOneById(courseId) match {
      case Some(course) => 
        Ok(views.html.teacherCourse(courseId, userId, course.title))
      case None => BadRequest("course does not exist")
    }
  }

  def unsubscribe(courseId: Long) = withUser { user => implicit request =>
    user.subscriptions = user.subscriptions.filter(_ != courseId)
    user.chapterSolutions = user.chapterSolutions.filter(_.courseId != courseId)
    userService.update(user)
    Redirect(routes.Application.dashboard)
  }

  def usersJson() = withUser { user => implicit request =>
    if (user.authority != "teacher") BadRequest(Json.obj("status" -> "KO"))

    val list = (userService.findAll().filter(_.authority == "student").map { 
      user =>

      JsObject(Map(
        "id" -> JsNumber(user.id), 
        "username" -> JsString(user.username),
        "subscriptions" -> Json.toJson(user.subscriptions),
        "progress" -> Json.toJson(user.subscriptions.map { s => 
          val course = courseService.findOneById(s)
          val chapterSolutions = user.chapterSolutions.filter(_.courseId == s)
          val progress = course match {
            case Some(c) => progressOf(c, chapterSolutions)
            case None => 0
          }
          JsArray(Seq(JsNumber(s), JsNumber(progress)))
        }),
        "chapterSolutions" -> Json.toJson(user.chapterSolutions)
      ))
    }).toSeq
    println(userService.findAll())
    Ok(Json.toJson(list))
  }

  def coursesJson() = Action {
    //val list = Services.courseService.findAll.map {
    //    course => JsObject(Map(
    //      "id" -> JsNumber(course.id), 
    //      "title" -> JsString(course.title)
    //    ))
    //}.toSeq
    var list = courseService.findAll().map {
      course => Json.toJson(course)
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
        // set id in case user send different
        val c = new Course(courseId, course.title, course.chapters)
        courseService.update(c) match {
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

    val chapterSolutionResult = request.body.validate[ChapterSolution]
    chapterSolutionResult.fold(
      errors =>  {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      sol => {
        // proof if tasks are solved
        sol.taskSolutions.foreach { ts =>
          courseService.findOneById(sol.courseId) match {
            case Some(course) => 
              course.chapters.find(_.id == sol.chapterId) match {
                case Some(chapter) => 
                  chapter.tasks.find(_.id == ts.taskId) match {
                    case Some(task) => 
                      task.solution match {
                        case Some(string) =>
                          ts.checked = Some(ts.taskState.isSolved(string))
                        case None => println("error solution")
                      }
                    case None => println("error no task")
                  }
                case None => println("error no chapter")
              }
            case None => println("error no course")
          }
        }

        //println("chapterSolution now: " + user.chapterSolutions)
        user.chapterSolutions = sol :: user.chapterSolutions.filter {
          x => x.courseId != sol.courseId || x.chapterId != sol.chapterId
        }
        //println("\nchapterSolution new: " + user.chapterSolutions)
        userService.update(user)
        Ok(Json.obj("status" -> "OK", "message" -> ("User updated")))
      }
    )
  }

  def solutionsJson(courseId: Long) = withUser { user => implicit request =>
    val solutions = user.chapterSolutions.filter(_.courseId == courseId)
    Ok(Json.toJson(solutions))
  }
  
  def deleteCourse(courseId: Long) = withUser { user => implicit request =>
    courseService.findOneById(courseId) match {
      case Some(course) =>
        courseService.delete(course)
        Ok(Json.obj("status" -> "OK", "message" -> "course deleted"))
      case None => Ok(Json.obj(
        "status" -> "KO", 
        "message" -> "could not delete course"
      ))
    }
  }

  case class InterpreterRequest(courseId: Long, chapterId: Long, taskId: String, 
    code: String)
  
  implicit val interpreterRequestReads: Reads[InterpreterRequest] = (
    (__ \ "courseId").read[Long] and
    (__ \ "chapterId").read[Long] and
    (__ \ "taskId").read[String] and
    (__ \ "code").read[String]
  )(InterpreterRequest.apply _)

  def interpretScala() = withUser(parse.json) { user => implicit request =>
    val irResult = request.body.validate[InterpreterRequest]
    irResult.fold(
      errors =>  {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      ir => {
        courseService.findOneById(ir.courseId) match {
          case Some(course) => 
            course.chapters.find(_.id == ir.chapterId) match {
              case Some(chapter) => 
                chapter.tasks.find(_.id == ir.taskId) match {
                  case Some(task) =>
                    try {
                      val code = ir.code + "\n" + task.solution.get
                      val result = Interpreter.run("scala", code)

                      Ok(Json.obj("status" -> "OK", "output" -> result.output))
                    } catch {
                      case e: Exception => 
                        println(e)
                        BadRequest(Json.obj(
                          "status"  -> "KO", 
                          "message" -> "could not interprete code"))
                    }
                  case None => BadRequest(Json.obj(
                    "status"  -> "KO", 
                    "message" -> ("task " + ir.taskId + " does not exist.")))
                }
              case None => BadRequest(Json.obj(
                "status"  -> "KO", 
                "message" -> ("chapter " + ir.chapterId + " does not exist.")))
            }
          case None => BadRequest(Json.obj(
            "status"  -> "KO", 
            "message" -> ("course " + ir.courseId + " does not exist.")))
        }
      }
    )
  }
}