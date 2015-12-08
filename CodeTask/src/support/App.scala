package support

import java.io.PrintWriter
import scala.io.Source._

object App {
  def main(args: Array[String]) {
    val al = fromFile("src/tasks/AboutLists.scala").mkString;
    val pw = new PrintWriter("title.json")
    
    val json = new Parser(al).parseToJson("About Scala Lists")
    pw.write(json)
    pw.close()
    println("finised")
  }
}