package models

trait UserRepository {
  def findOneByUsername(username: String): Option[User]
  def findOneById(id: Long): Option[User]
  def findAll(): List[User]
  def create(user: User): Option[User]
  def update(user: User): Option[User]
  def delete(user: User): Option[User]
}