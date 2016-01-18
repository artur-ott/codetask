import play.api._
import models.Services.userService
import models.User

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val administrators = List(
      User(userService.newId(), "admin@a.pp", "admin", userService.passwordHash("$1amn_$2pwrt"))
    )

    administrators.foreach { user =>
      userService.create(user) match {
        case Some(newUser) => Logger.info("user " + newUser.id + " created")
        case None => //Logger.info("could not create user " + user.id)
      }
    }
  }
}