package models

trait UserRepository {
	def findOneByUsername(username: String): Option[User]
	def create(user: User): Option[User]
	def update(user: User): Option[User]
	def deleteOneByUsername(username: String): Option[User]
}