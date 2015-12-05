package tasks

import support.CodeTaskSuite
import org.scalatest.Matchers

class AboutLists extends CodeTaskSuite {
  // keine tabs und indentation 2 space oder nur tabs
  video("Eine Erklärung der Grundlagen in scala", "http://youtube/watch?lpk42")
  
  koan("das ist ein koan eine aufgabe mit fehlenden assert werten") {
  	var x = 1
  	x should be(1)
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
      //this
  	}
  
  	//test
  	rvrs(List(1, 2, 3)) should be(List(3, 2, 1))
  	//this
  }  
}