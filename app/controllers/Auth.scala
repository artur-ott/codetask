package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import models.Services.userService

class Auth extends Controller {

  case class UserData(email: String, password: String)
  val registerForm = Form(
      tuple(
        "email" -> email,
        "password" -> nonEmptyText,
        "password2" -> nonEmptyText
      )  verifying ("Passwords don't match", result => result match {
         case (email, password, password2) => password == password2
      })
  )

  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => check(email, password)
    })
  )

  def exists(username: String): Boolean = {
    userService.findOneByUsername(username) != None
  }

  def check(username: String, password: String): Boolean = {
    userService.findOneByUsername(username) match {
      case Some(user) => userService.checkPassword(password, user.password)
      case None => false
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def register() = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }

  def submit() = Action { implicit request =>
    registerForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.register(formWithErrors)),
        user => {
          if (!exists(user._1)) {
            val id = userService.newId()
            val u = new User(id, user._1, "student", userService.passwordHash(user._2))
            userService.create(u) 
            Redirect(routes.Application.dashboard).withSession(Security.username -> user._1)
          } else {
            Redirect(routes.Auth.register).flashing(
              "failure" -> "email already exists."
            )
          }
        }
      )
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.dashboard).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}