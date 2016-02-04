package models

import org.mindrot.jbcrypt.BCrypt
import scala.language.reflectiveCalls

class UserService(env: {val userRepository: UserRepository}) { 
  def create(user: User): Option[User] = env.userRepository.create(user) 
  def update(user: User): Option[User] = env.userRepository.update(user)
  def delete(user: User) = env.userRepository.delete(user)
  def findAll(): List[User] = env.userRepository.findAll()
  def findOneById(id: Long): Option[User] =  env.userRepository.findOneById(id)
  def findOneByUsername(username: String): Option[User] = 
    env.userRepository.findOneByUsername(username)
  def newId(): Long = {
    val users = findAll()
    var id = 200000
    do { id += 1 } while (users.find(u => u.id == id) != None)
    id
  }
  def passwordHash(password: String): String = 
    BCrypt.hashpw(password, BCrypt.gensalt())
  def checkPassword(password: String, passwordHash: String): Boolean =
    BCrypt.checkpw(password, passwordHash)
}