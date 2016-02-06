package models

import models.db4o._
import models.dummydb._

object Config {
  lazy val userRepository = new UserRepositoryDummy
  lazy val courseRepository = new CourseRepositoryDummy
}