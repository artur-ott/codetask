package models.hibernate

import models._
import javax.persistence.Persistence
import javax.persistence.EntityManagerFactory
import org.hibernate._
import org.hibernate.ejb.HibernateEntityManagerFactory
import scala.collection.JavaConversions._
import play.db.jpa.JPA 


class CourseRepositoryHibernate() extends CourseRepository {
  val entityManagerFactory: EntityManagerFactory = 
    Persistence.createEntityManagerFactory("defaultPersistenceUnit")
  val factory = 
    (entityManagerFactory.asInstanceOf[HibernateEntityManagerFactory])
    .getSessionFactory()

  def toCourseList(list: java.util.List[_]): List[Course] = {
    val courseHList = list.asInstanceOf[java.util.List[CourseHibernate]].toList
    courseHList.map{u => u.toCourse}
  }

  def stackTraceString(e: Exception): String = {
    val sw = new java.io.StringWriter()
    e.printStackTrace(new java.io.PrintWriter(sw))
    sw.toString()
  }

  def create(course: Course): Option[Course] = {
    var result: Option[Course] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from CourseHibernate c where c.title = " + 
        "'" + course.title + "'"
        val list = sess.createQuery(query).list()

        if (list.size == 0) {
          val courseH = ( new CourseHibernate ).fill(course)
          sess.persist(courseH)
          course.id = courseH.id
          result = Some(course)
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }

  def update(course: Course): Option[Course] = {
    var result: Option[Course] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from CourseHibernate c where c.title = " + 
        "'" + course.title + "' and c.id = " + course.id
        val list = sess.createQuery(query).list()

        if (list.size > 0) {
          val courseH = list.asInstanceOf[java.util.List[CourseHibernate]].get(0)
          sess.update(courseH.fill(course))
          sess.flush()
          result = Some(course)
        }

        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback()
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }

  def delete(course: Course): Option[Course] = {
    var result: Option[Course] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
       
        val query = "from CourseHibernate c where c.title = " + 
        "'" + course.title + "' and c.id = " + course.id
        val list = sess.createQuery(query).list()

        if (list.size > 0) {
          val courseH = list.asInstanceOf[java.util.List[CourseHibernate]].get(0)
          sess.delete(courseH)
          result = Some(course)
        }

        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }

  def findOneByTitle(title: String): Option[Course] = {
    var result: Option[Course] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from CourseHibernate c where c.title = " + 
          "'" + title + "'"
        val list = sess.createQuery(query).list()
        list.isEmpty match {
          case false => 
            result = Some(toCourseList(list).head)
          case true => 
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback()
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }

 def findOneById(id: Long): Option[Course] = {
    var result: Option[Course] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from CourseHibernate c where c.id = " + id
        val list = sess.createQuery(query).list()
        list.isEmpty match {
          case false => result = Some(toCourseList(list).head)
          case true => 
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback()
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }

  def findAll(): List[Course] = {
    var result: List[Course] = List()
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val list = sess.createCriteria(classOf[CourseHibernate]).list()
        result = toCourseList(list)
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(stackTraceString(e))
    }
    finally {
        sess.close()
    }
    result
  }
}