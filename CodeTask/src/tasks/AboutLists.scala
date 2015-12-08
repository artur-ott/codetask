package tasks

import support.CodeTaskSuite
import org.scalatest.Matchers

class AboutLists extends CodeTaskSuite {
  // keine tabs und indentation 2 space oder nur tabs
  video("""In diesem Kapitel sollend Listen in Scala näher erläutert werden
         | Listen sind collections und können objekte speichern
         | Listen sind prinzipiell immutable also unveränderbar
         | Im folgenden Video werden Listen ausfürlich erläutert""", "U23j6yH21W4")
  
  koan("""Mit der Funktion <pre>contains</pre> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.
        | Mit der Funktion <pre>map</pre> können funktionen auf listen angewendet werden, die Ergebnisse werden in einer neuen Liste gespeichert.
        | Versuch in dem folgenden <pre>Koan</pre> die richtigen Werte einzutragen""") {
    val l = List(1, 2, 3, 4)
    val l2 = l.map { x => x + 1 }
    val l3 = l.map { x => x * x }
    
    l should be (List(1, 2, 3, 4))
    l2 should be(List(2, 3, 4, 5))
    l3 shouldBe List(1, 4, 9, 16)
  }
  
  koan("Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <pre>++</pre> geschehen.") {
    val l = List(1, 3, 5)
    val l2 = l ++ List(6)
    
    l2 shouldBe List(1, 3, 5, 6)
  }
  
  codetask("""schreiben sie eine function reverse die eine umgekehrte liste zurück geben.
            | Nutzen Sie nicht die bereits vorhandenen Möglichkeit
            | List.reverse""") {
    def rvrs(l: List[Any]): List[Any] = {
      //solve
      l match {
        case h :: tail => rvrs(tail) ::: List(h)
        case _       => Nil
      }
      //endsolve
    }
  
    //test
    rvrs(List(1, 2, 3)) should be(List(3, 2, 1))
    //endtest
  }  
}