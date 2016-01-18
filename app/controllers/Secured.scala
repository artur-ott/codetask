package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._ 
import play.api.data._
import play.api.data.Forms._
import models._
import models.Services.userService

trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    Services.userService.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  // with BodyParser

  def withUser[A](bp: BodyParser[A])(f: User => Request[A] => Result) = withAuth(bp) { username => implicit request =>
    Services.userService.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  def withAuth[A](bp: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(bp)(request => f(user)(request))
    }
  }


  // Basic Authorisation from https://gist.github.com/guillaumebort/2328236

  def withBasicAuth[A](bp: BodyParser[A])(authoritys: List[String])(f: Request[A] => Result) = Action(bp) { implicit request =>
    request.headers.get("Authorization").flatMap { authorization =>
      authorization.split(" ").drop(1).headOption.filter { encoded =>
        new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
          case c :: s :: Nil => userService.findOneByUsername(c) match {
              case Some(user) => userService.checkPassword(s, user.password) && authoritys.contains(user.authority)
              case None => false
            }
          case _ => false
        }
      }.map(_ => f(request))
    }.getOrElse {
      Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=\"Secured\"")
    }
  }
}