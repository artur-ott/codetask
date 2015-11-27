package models

object Config {
	lazy val userRepository = new UserRepositoryDb4o
}