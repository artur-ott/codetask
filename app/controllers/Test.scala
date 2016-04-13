package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api._
import play.api.data._
import play.api.mvc._

//class Test @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {
class Test extends Controller {
	def index = Action { implicit request =>
		Ok("" + Messages("test.test") + play.i18n.Messages.get("test.test"))
    }

    def view = Action { implicit request =>
    	Ok(views.html.testView())
    }
}