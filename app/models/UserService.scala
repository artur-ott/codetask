 package models

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