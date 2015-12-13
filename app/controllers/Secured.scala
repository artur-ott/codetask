package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

   def withAuth[A](bp: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(bp)(request => f(user)(request))
    }
  }

  def withUser[A](bp: BodyParser[A])(f: User => Request[A] => Result) = withAuth(bp) { username => implicit request =>
    Services.userService.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    Services.userService.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}