package models.dummydb

import models._

class UserRepositoryDummy extends UserRepository {
  var db: List[User] = List()

  def create(user: User): Option[User] = {
    db.find{ x => x.id == user.id || x.username == user.username} match {
      case Some(found) => None
      case _ => db = db ++ List(user); Some(user)
    }
  }

  def update(user: User): Option[User] = {
    db.find{ x => x.id == user.id && x.username == user.username} match {
      case Some(found) => db = db.filter(_.id != user.id) ++ List(user); Some(user)
      case _ => None
    }
  }

  def delete(user: User): Option[User] = {
    db.find{ x => x.id == user.id && x.username == user.username} match {
      case Some(found) => db = db.filter(_.id != user.id); Some(user)
      case _ => None
    }
  }
  def findOneByUsername(username: String): Option[User] = {
    db.find(_.username == username)
  }
  
  def findOneById(id: Long): Option[User] = {
    db.find(_.id == id)
  }

  def findAll(): List[User] = {
    db
  }

}

