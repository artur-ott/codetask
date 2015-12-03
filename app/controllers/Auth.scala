package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{User}

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
    Services.userService.findOneByUsername(username) != None
  }

  def check(username: String, password: String): Boolean = {
    val result = Services.userService.findOneByUsername(username)
    var valid = false
    if (result != None) {
      valid = result.get.password == password
    }
    valid
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
            Services.userService.create(new User(user._1, "student", user._2)) 
            Redirect(routes.Application.dashboard).withSession(Security.username -> user._1)
          } else {
            Redirect(routes.Auth.register).flashing(
              "failure" -> "Username / email already exists."
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