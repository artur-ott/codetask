import play.api._
import models.Services.userService
import models.User
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._


object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val administrators = List(
      User(User.NEW, "admin@a.pp", "admin", userService.passwordHash("$1amn_$2pwrt"))
    )

    administrators.foreach { user =>
      userService.create(user) match {
        case Some(newUser) => Logger.info("user " + newUser.id + " created")
        case None => //Logger.info("could not create user " + user.id)
      }
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(
      "Error"
    ))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      "404 Not Found"
    ))
  }
}

class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError("A server error occurred: " + exception.getMessage)
    )
  }
}