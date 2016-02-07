package models.hibernate

import models._
import javax.persistence.Persistence
import javax.persistence.EntityManagerFactory
import org.hibernate._
//import org.hibernate.EntityManagerFactory
import org.hibernate.ejb.HibernateEntityManagerFactory
import scala.collection.JavaConversions._
import play.db.jpa.JPA 


class UserRepositoryHibernate extends UserRepository {
  val entityManagerFactory: EntityManagerFactory = 
    Persistence.createEntityManagerFactory("defaultPersistenceUnit")
  val factory = 
    (entityManagerFactory.asInstanceOf[HibernateEntityManagerFactory])
    .getSessionFactory()

  def toUserList(list: java.util.List[_]): List[User] = {
    val userHList = list.asInstanceOf[java.util.List[UserHibernate]].toList
    userHList.map{u => u.toUser}
  }

  def create(user: User): Option[User] = {
    var result: Option[User] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val userH = ( new UserHibernate ).fill(user)
        val users = findAll()
        if (users.find{u => u.id == user.id || 
          u.username == user.username}.isDefined) {

          result = None
        } else {
          sess.persist(userH)
          result = Some(user)
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }

  def update(user: User): Option[User] = {
    var result: Option[User] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val userH = ( new UserHibernate ).fill(user)
        val users = findAll()
        if (users.find{u => u.id == user.id || 
          u.username == user.username}.isDefined) {

          sess.update(userH)
          result = Some(user)
        } else {
          result = None
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }

  def delete(user: User): Option[User] = {
    var result: Option[User] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val users = findAll()
        users.find{u => u.id == user.id || u.username == user.username} match {
          case Some(user) =>
            val query = "delete from UserHibernate where id = " + user.id
            sess.createQuery(query).executeUpdate()
            result = Some(user)
          case None =>
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }

  def findOneByUsername(username: String): Option[User] = {
    var result: Option[User] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from UserHibernate u where u.username = " + 
          "'" + username + "'"
        val list = sess.createQuery(query).list()
        list.isEmpty match {
          case false => 
            result = Some(toUserList(list).head)
          case true => 
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback()
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }
  
  def findOneById(id: Long): Option[User] = {
    var result: Option[User] = None
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val query = "from UserHibernate u where u.id = " + id
        val list = sess.createQuery(query).list()
        list.isEmpty match {
          case false => 
            result = Some(toUserList(list).head)
          case true => 
        }
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback()
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }

  def findAll(): List[User] = {
    var result: List[User] = List()
    val sess = factory.openSession()
    var tx: Transaction = null
    try {
        tx = sess.beginTransaction()
        val list = sess.createCriteria(classOf[UserHibernate]).list()
        result = toUserList(list)
        tx.commit()
    }
    catch {
        case e: Exception => 
          if (tx!=null) tx.rollback() 
          play.Logger.info(e.getMessage)
    }
    finally {
        sess.close()
    }
    result
  }

}

