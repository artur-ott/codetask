package models

import models.db4o._

object Config {
  lazy val userRepository = new UserRepositoryDb4o
  lazy val courseRepository = new CourseRepositoryDb4o
}