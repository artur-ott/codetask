/* UNMAINTAINED

package models.db4o

import com.db4o.Db4o
import com.db4o.reflect.jdk.JdkReflector
import com.db4o.ObjectContainer
import com.db4o.query.Predicate

object Util {
  com.db4o.Db4o.configure()
    .reflectWith(new JdkReflector(this.getClass().getClassLoader()))
  val objectServer = Db4o.openServer("database.data", 0)

  implicit def objCont2QueryHelper(oc: ObjectContainer) = new QueryHelper(oc)
}

// borrowed http://ted-gao.blogspot.de/2012/12/using-db4o-in-scala-programs.html
class QueryHelper (connection: ObjectContainer) {
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

*/