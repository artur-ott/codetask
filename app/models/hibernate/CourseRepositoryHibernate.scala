package models.hibernate

import models._

class CourseRepositoryHibernate() extends CourseRepository {

  var db: List[Course] = List()

  def findOneByTitle(name: String): Option[Course] = {
    db.find(_.title == name)
  }

  def findOneById(id: Long): Option[Course] = {
    db.find(_.id == id)
  }

  def findAll(): List[Course] = {
    db
  }

  def create(course: Course): Option[Course] = {
    db.find{ x => x.id == course.id || x.title == course.title} match {
      case Some(found) => None
      case _ => db = db ++ List(course); Some(course)
    }
  }

  def update(course: Course): Option[Course] = {
    db.find{ x => x.id == course.id && x.title == course.title} match {
      case Some(found) => db = db.filter(_.id != course.id) ++ List(course); Some(course)
      case _ => None
    }
  }

  def delete(course: Course): Option[Course] = {
    db.find{ x => x.id == course.id && x.title == course.title} match {
      case Some(found) => db = db.filter(_.id != course.id); Some(course)
      case _ => None
    }
  }
}