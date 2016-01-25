package models.db4o

import com.db4o.Db4o
import models._
import Util.objCont2QueryHelper
import Util.objectServer

class UserRepositoryDb4o extends UserRepository {
  def create(user: User): Option[User] = {
    val client = objectServer.openClient();
    client query {u: User => 
      u.username == user.username || u.id == user.id} match {

      case x :: xs => None
      case _ => 
        client.store(user)
        client.commit()
        client.close()
        Some(user)
    }
  }

  def update(user: User): Option[User] = {
    var client = objectServer.openClient();
    client query {u: User => 
      u.username == user.username && u.id == user.id} match {
        
      case x :: xs => 
        client.delete(x)
        client.store(user)
        client.commit()
        client.close(); 
        Some(x)
      case _ => client.close(); None
    }
  }

  def delete(user: User): Option[User] = {
    val client = objectServer.openClient();
    client query {u: User => 
      u.username == user.username && u.id == user.id} match {

      case x :: xs => 
        client.delete(x)
        client.commit()
        client.close()
        Some(x)
      case _ => client.close(); None
    }
  }
  def findOneByUsername(username: String): Option[User] = {
    var client = objectServer.openClient();
    client query {user: User => user.username == username} match {
      case x :: xs => client.close(); Some(x)
      case _       => client.close(); None
    }
  }
  
  def findOneById(id: Long): Option[User] = {
    var client = objectServer.openClient();
    client query {user: User => user.id == id} match {
      case x :: xs => client.close(); Some(x)
      case _       => client.close(); None
    }
  }

  def findAll(): List[User] = {
    val client = objectServer.openClient();
    client query {user: User => true}
  }

}

