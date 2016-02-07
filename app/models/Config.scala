package models

import models.db4o._
import models.dummydb._
import models.hibernate._

object Config {
  lazy val userRepository = new UserRepositoryHibernate
  lazy val courseRepository = new CourseRepositoryHibernate
}