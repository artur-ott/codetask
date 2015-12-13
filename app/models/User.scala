package models

import play.api.libs.json._

// courses: Map[coursename, Map[chaptername, Map[taskname, solution]]]
class User(var username: String, var authority: String, var password: String, var courses: Map[String, Map[String, Map[String, JsObject]]] = Map())

trait UserRepository {
	def findOneByUsername(username: String): Option[User]
	def findAll(): List[User]
	def create(user: User): Option[User]
	def update(user: User): Option[User]
	def delete(user: User): Option[User]
}

 class UserService(env: {val userRepository: UserRepository}) { 
 	def findOneByUsername(username: String): Option[User] = 
 		env.userRepository.findOneByUsername(username)
 	def findAll() =
 		env.userRepository.findAll()
 	def create(user: User): Option[User] =
 		env.userRepository.create(user)
 	def update(user: User): Option[User] =
 		env.userRepository.update(user)
 	def delete(user: User): Option[User] =
 		env.userRepository.delete(user)
 }