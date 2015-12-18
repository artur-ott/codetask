package models

// on success methods return User Object else None
trait UserRepository {
  def newId(): Long
  // return User if created return None if user with id and username exists already
  def create(user: User): Option[User]
  // return User if updated return None if user with id and username not found
  def update(user: User): Option[User]
  // return User if deleted return None if user with id and username not found
  def delete(user: User): Option[User]
  def findAll(): List[User]
  def findOneById(id: Long): Option[User]
  def findOneByUsername(username: String): Option[User]
}