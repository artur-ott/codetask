package models

class User(var username: String, var authority: String, var password: String, var courses: List[String])

trait UserRepository {
	def findOneByUsername(username: String): Option[User]
	def create(user: User): Option[User]
	def update(user: User): Option[User]
	def delete(user: User): Option[User]
}

 class UserService(env: {val userRepository: UserRepository}) { 
 	def findOneByUsername(username: String): Option[User] = 
 		env.userRepository.findOneByUsername(username)
 	def create(user: User): Option[User] =
 		env.userRepository.create(user)
 	def update(user: User): Option[User] =
 		env.userRepository.update(user)
 	def delete(user: User): Option[User] =
 		env.userRepository.delete(user)
 }