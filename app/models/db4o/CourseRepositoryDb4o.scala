package models.db4o

import com.db4o.Db4o
import models._
import Util.objCont2QueryHelper
import Util.objectServer

class CourseRepositoryDb4o() extends CourseRepository {
  def findOneByTitle(name: String): Option[Course] = {
    var client = objectServer.openClient();
    client query {course: Course => course.title == name} match {
      case x :: xs => client.close(); Some(x)
      case _       => client.close(); None
    }
  }

  def findOneById(id: Long): Option[Course] = {
    var client = objectServer.openClient();
    client query {course: Course => course.id == id} match {
      case x :: xs => client.close(); Some(x)
      case _       => client.close(); None
    }
  }

  def findAll(): List[Course] = {
    val client = objectServer.openClient();
    client query {course: Course => true}
  }

  def create(course: Course): Option[Course] = {
    val client = objectServer.openClient();
    client query {c: Course => 
      c.title == course.title || c.id == course.id} match {
      
      case x :: xs => None
      case _       => 
        client.store(course)
        client.commit()
        client.close()
        Some(course)
    }
  }

  def update(course: Course): Option[Course] = {
    var client = objectServer.openClient();
    client query {c: Course => 
      c.title == course.title && c.id == course.id} match {
      
      case x :: xs => 
        x.title = course.title
        x.chapters = course.chapters
        client.store(course)        
        client.commit()
        client.close(); 
        Some(x)
      case _       => client.close(); None
    }
  }

  def delete(course: Course): Option[Course] = {
    val client = objectServer.openClient();
    client query {c: Course => c.id == course.id} match {
       case x :: xs => 
          client.delete(x)
          client.commit()
          client.close()
          Some(x)
       case _       => client.close(); None
    }
  }
}