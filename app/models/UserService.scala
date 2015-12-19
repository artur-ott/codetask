package models

class UserService(env: {val userRepository: UserRepository}) { 
  def newId(): Long = env.userRepository.newId()
  def create(user: User): Option[User] = env.userRepository.create(user) 
  def update(user: User): Option[User] = env.userRepository.update(user)
  def delete(user: User) = env.userRepository.delete(user)
  def findAll(): List[User] = env.userRepository.findAll()
  def findOneById(id: Long): Option[User] =  env.userRepository.findOneById(id)
  def findOneByUsername(username: String): Option[User] = 
    env.userRepository.findOneByUsername(username)
}