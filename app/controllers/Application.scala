package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.JsValue._
import scala.collection.JavaConverters._
import models._
import models.Services._
import models.Course._
import models.User._

class Application extends Controller with Secured {
  val sta = List("student", "teacher", "admin")
  val ta = List("teacher", "admin")
  val a = List("admin")

  def index() = Action {
    Redirect(routes.Auth.login)
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

    if (user.authority != "teacher" && user.authority != "admin")
      BadRequest("no authority")

    courseService.findOneById(courseId) match {
      case Some(course) => 
        Ok(views.html.teacherCourse(courseId, userId, course.title))
      case None => NotFound("course does not exist")
    }
  }

  def unsubscribe(courseId: Long) = withUser { user => implicit request =>
    user.subscriptions = user.subscriptions.filter(_ != courseId)
    user.chapterSolutions = user.chapterSolutions.filter(_.courseId != courseId)
    userService.update(user)
    Redirect(routes.Application.dashboard)
  }

  def createCourse() = withBasicAuth(parse.json)(ta) { 
    implicit request =>

    println(request.body.toString())
    request.body.validate[Course].fold(
      errors => {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      course => {
        courseService.create(course) match {
          case Some(saved) => Created(Json.obj(
            "status" -> "OK", 
            "message" -> ("Course '" + saved.title + "' created")
          )).withHeaders(LOCATION -> ("/api/courses/" + course.id))
          case None =>  Conflict(Json.obj(
            "status" -> "KO", 
            "message" -> ("Course '" + course.title + "' already exists")
          ))
        }
      }
    )
  }

  def getCourse(courseId: Long) = Action {
    courseService.findOneById(courseId) match {
      case Some(course) => Ok(Json.toJson(course))
      case None => NotFound(Json.obj(
        "status" -> "KO", 
        "message" -> ("Course '" + courseId + "' not found")
      ))
    }
  }

  def updateCourse(courseId: Long) = withBasicAuth(parse.json)(ta) { 
    implicit request =>

    request.body.validate[Course].fold(
      errors => {
        BadRequest(Json.obj(
          "status" -> "KO",
          "message" -> JsError.toJson(errors)
        ))
      },
      course => {
        val c = new Course(courseId, course.title, course.chapters)
        courseService.update(c) match {
          case Some(saved) =>  Ok(Json.obj(
            "status" -> "OK", 
            "message" -> ("Course '" + saved.title + "' saved")
          ))
          case None =>  NotFound(Json.obj(
            "status" -> "KO", 
            "message" -> ("Course id: '" + courseId + "' does not exists")
          ))
        }
      }
    )
  }

  def deleteCourse(courseId: Long) = withBasicAuth(parse.anyContent)(ta) { 
    implicit request =>
    
    courseService.findOneById(courseId) match {
      case Some(course) =>
        courseService.delete(course)
        Ok(Json.obj("status" -> "OK", "message" -> "course deleted"))
      case None => NotFound(Json.obj(
        "status" -> "KO", 
        "message" -> ("Course id: '" + courseId + "' does not exists")
      ))
    }
  }

  def getCourses() = Action {
    var list = courseService.findAll().map {
      course => Json.toJson(course)
    }.toSeq
    Ok(Json.toJson(list))
  }

  def createUser() = withBasicAuth(parse.json)(a) {
    implicit request =>

    request.body.validate[User].fold(
      errors =>  {
        BadRequest(Json.obj(
          "status" -> "KO", 
          "message" -> JsError.toJson(errors)
        ))
      },
      user => {
        user.password = userService.passwordHash(user.password)
        userService.create(user) match {
          case Some(saved) => Created(Json.obj(
            "status" -> "OK", 
            "message" -> ("User '" + saved.username + "' created")
          )).withHeaders(LOCATION -> ("/api/users/" + user.id))
          case None =>  Conflict(Json.obj(
            "status" -> "KO", 
            "message" -> ("User '" + user.username + "' already exists")
          ))
        }
      }
    )
  }

  def getUser(userId: Long) = withBasicAuth(parse.anyContent)(ta) { 
    implicit request =>

    userService.findOneById(userId) match {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj(
        "status" -> "KO", 
        "message" -> ("User '" + userId + "' not found")
      ))
    }
  }

  def updateUser(userId: Long) = withBasicAuth(parse.json)(ta) { 
    implicit request =>

    request.body.validate[User].fold(
      errors => {
        BadRequest(Json.obj(
          "status" -> "KO",
          "message" -> JsError.toJson(errors)
        ))
      },
      user => {
        val u = new User(
          userId, 
          user.username,
          user.authority, 
          userService.passwordHash(user.password),
          user.chapterSolutions,
          user.subscriptions
        )

        userService.update(u) match {
          case Some(saved) =>  Ok(Json.obj(
            "status" -> "OK", 
            "message" -> ("User '" + saved.username + "' saved")
          ))
          case None =>  NotFound(Json.obj(
            "status" -> "KO", 
            "message" -> ("User id: '" + userId + "' does not exists")
          ))
        }
      }
    )
  }

  def deleteUser(userId: Long) = withBasicAuth(parse.anyContent)(a) { 
    implicit request =>
    
    userService.findOneById(userId) match {
      case Some(user) =>
        userService.delete(user)
        Ok(Json.obj("status" -> "OK", "message" -> "user deleted"))
      case None => NotFound(Json.obj(
        "status" -> "KO", 
        "message" -> ("User id: '" + userId + "' does not exists")
      ))
    }
  }

  def getUsers() =  withBasicAuth(parse.anyContent)(a) { 
    implicit request =>

    var list = userService.findAll().map {
      course => Json.toJson(course)
    }
    Ok(Json.toJson(list.toSeq))
  }

  def getStudents() = //withBasicAuth(parse.anyContent)(ta) {
    //implicit request =>
    withUser(parse.anyContent) { 
    user => implicit request =>


    val list = userService.findAll().filter(_.authority == "student").map { 
      user =>

      JsObject(Map(
        "id" -> JsNumber(user.id), 
        "username" -> JsString(user.username),
        "subscriptions" -> Json.toJson(user.subscriptions),
        "progress" -> Json.toJson(user.subscriptions.map { s => 
          val chapterSolutions = user.chapterSolutions.filter(_.courseId == s)
          val progress = courseService.findOneById(s) match {
            case Some(c) => progressOf(c, chapterSolutions)
            case None => 0
          }
          JsArray(Seq(JsNumber(s), JsNumber(progress)))
        }),
        "chapterSolutions" -> Json.toJson(user.chapterSolutions)
      ))
    }
    Ok(Json.toJson(list.toSeq))
  }

  // shell client
  def getUserTable() =  withBasicAuth(parse.anyContent)(a) { 
    implicit request =>

    val users = userService.findAll()
    val list = users.map{ u => 
      val fs = "%s%" + (30 - u.username.length) + "s"
      fs format (u.username, u.id) 
    }.mkString("\n") 
    Ok(list)
  }

  def getCourseTable() =  withBasicAuth(parse.anyContent)(ta) { 
    implicit request =>

    val courses = courseService.findAll()
    val list = courses.map{ c => 
      val fs = "%s%" + (30 - c.title.length) + "s"
      fs format (c.title, c.id)
    }.mkString("\n")
    Ok(list)
  }


  def storeSolution(courseId: Long) = withUser(parse.json) { 
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
        var failed: Option[String] = None

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
                        case None => failed = Some("error solution")
                      }
                    case None => failed = Some("error no task")
                  }
                case None => failed = Some("error no chapter")
              }
            case None => failed = Some("error no course")
          }
        }

        if (failed == None) {
          user.chapterSolutions = sol :: user.chapterSolutions.filter {
            x => x.courseId != sol.courseId || x.chapterId != sol.chapterId
          }
          userService.update(user)
          Ok(Json.obj("status" -> "OK", "message" -> "User updated"))
        } else {
          BadRequest(Json.obj("status" -> "KO", "message" -> failed.get))
        }
      }
    )
  }

  def getSolution(courseId: Long) = withUser { user => implicit request =>
    val solutions = user.chapterSolutions.filter(_.courseId == courseId)
    Ok(Json.toJson(solutions))
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

                      Ok(Json.obj(
                        "status" -> "OK", 
                        "output" -> result.output,
                        "success" -> JsBoolean(result.success)))
                    } catch {
                      case e: Exception => 
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