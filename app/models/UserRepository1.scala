package models

import com.db4o.Db4o

class UserRepository1 extends UserRepository {
	def findOneByUsername(username: String): Option[User] = {
 		var client = UserRepository1.objectServer.openClient();

 		new A(client) query {user: User => user.username == username} match {
 			case x :: xs => client.close(); Some(x)
 			case _       => client.close(); None
 		}
	}

	def create(user: User): Option[User] = {
 		val client = UserRepository1.objectServer.openClient();
 		new A(client) query {u: User => u.username == user.username} match {
 			case x :: xs => None
 			case _       => 
 				client.store(user)
				client.commit()
				client.close()
				Some(user)
 		}
	}

	def update(user: User): Option[User] = {
 		var client = UserRepository1.objectServer.openClient();

 		new A(client) query {u: User => u.username == user.username} match {
 			case x :: xs => 
 				x.username = user.username
 				x.authority = user.authority
 				x.password = user.password
 				client.store(x)
 				client.commit()
 				client.close(); 
 				Some(x)
 			case _       => client.close(); None
 		}
	}

	def deleteOneByUsername(username: String): Option[User] = {
		val client = UserRepository1.objectServer.openClient();

 		new A(client) query {user: User => user.username == username} match {
 			case x :: xs => 
 				client.delete(x)
 				client.commit()
 				client.close()
 				Some(x)
 			case _       => client.close(); None
 		}
	}
}

object UserRepository1 {
	val objectServer = Db4o.openServer("codetask.data", 0)
}

// borrowed http://ted-gao.blogspot.de/2012/12/using-db4o-in-scala-programs.html
// adds Adapter
import com.db4o.ObjectContainer
import com.db4o.query.Predicate
 
class A (connection: ObjectContainer) {
    def query[T](predicate: T => Boolean) : List[T] = {
        var results : List[T] = List[T]()
        val objectSet = connection.query(new Predicate[T]() {
            override
            def `match`(entry: T) = {
                predicate(entry)
            }
        });
         
        while (objectSet.hasNext()) {
            results = objectSet.next :: results
        }
         
        results
    }
}
// /borrowed