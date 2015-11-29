package support

import tasks._
import org.scalatest._
import scala.util.matching.Regex
import scala.util.control.Exception
import scala.collection.immutable.TreeMap

case class MatchException(smth:String)  extends Exception(smth)

object CodeTasks extends App {
  println()
}

class Parser(s: String) {
  var videos = 0;
  var koans = 0;
  var codetasks = 0;
  var map = Map[(String, Int), Map[String, String]]()
  //var map = TreeMap[Int, (String, Map[String, String])]()
  
  val video = """video\s*\(\s*(\"\"\"([\s\S]*)\"\"\"|\"(.+)\")(\s*,?\s*)(\"\"\"(.+)\"\"\"|\"(.+)\")\s*\)""".r
  val koan = """koan\s*\(\s*(\"\"\"([\s\S]*)\"\"\"|\"(.+)\"\s*\)(\s*\{))""".r
  val codetask = """codetask\s*\(\s*(\"\"\"([\s\S]*)\"\"\"|\"(.+)\"\s*\)(\s*\{))""".r
  val assert = """((should\sequal\s*)|(should\s*===\s*)|(should\s*be\s*)|(shouldEqual\s*)|(shouldBe\s*))((\((.*)\))|((.*)))""".r
  val clean = """^\s*(\S[\s\S]*\S)\s""".r
  
  def parseVideos {
    val matches = video findAllMatchIn s
    matches.foreach { m =>
      val index = s.indexOf(m.toString)
      videos += 1
      map = map + (("video" + videos, index) -> Map("description" -> m.group(3).stripMargin('|'), "url" -> m.group(7)))
    }
  }
  
  
  /* Possible asserts in koans
   *                         // asserts must be on one line
   * result should equal (3) // can customize equality
	 * result should === (3)   // can customize equality and enforce type constraints
	 * result should be (3)    // cannot customize equality, so fastest to compile
	 * result shouldEqual 3    // can customize equality, no parentheses required
	 * result shouldBe 3       // cannot customize equality, so fastest to compile, no parentheses required
   * */
  
  def parseKoans {
    val matches = koan findAllMatchIn s
    var index = 0
    matches.foreach { m =>
      index = s.indexOf(m.toString, index)
      koans += 1
      
      val description = m.group(3).stripMargin('|')
      val pos = parseCurlyBraces(s, index + m.toString.size - 1)
      // get code and remove whitespace before and after
      var code = ""
      clean findFirstMatchIn (s.slice(pos._1 + 1, pos._2)) foreach { m2 => code = m2.group(1).toString }
      
      // retrieve all solutions and replace them with __
      var solutions = List[String]()
      assert findAllMatchIn code foreach { m2 => 
        // get all matched assert types
        val groups = List(2, 3, 4, 5, 6) filter { g => m2.group(g) != null }
        groups foreach { g =>
          var solution = ""
          var replace = "__"
          // get solution from either (__) or __
          if (m2.group(9) != null) {
            solution = m2.group(9).toString();
            replace = "(__)"
          } else {
            solution = m2.group(11).toString();
          }
          solutions = solutions ::: List(solution)
          // replace solution in code
          code = code.replace(m2.toString, m2.group(g).toString() + replace)
        } 
      }
      
      map = map + (("koan" + koans, index) -> Map("description" -> description, "code" -> code.toString, "solutions" -> solutions.mkString(";")))
      
      // add one so the next match is processed
      index += 1
    }
  }
  
  def parseCodeTasks {
    val matches = codetask findAllMatchIn s
    var index = 0
    matches.foreach { m =>
      index = s.indexOf(m.toString, index)
      codetasks += 1
      
      val description = m.group(3).stripMargin('|')
      val pos = parseCurlyBraces(s, index + m.toString.size - 1)
      // get code and remove whitespace before and after
      var code = ""
      clean findFirstMatchIn (s.slice(pos._1 + 1, pos._2)) foreach { m2 => code = m2.group(1).toString }
      
      // remove everything between //solve and //endsolve
      val solve = """[\s\S]*(\/\/solve\s*[\s\S]*\/\/endsolve)""".r
      val solveMatches = solve findAllMatchIn code
      if (solveMatches.isEmpty) throw MatchException("//solve / //endsolve did not match\n perhaps you are missing one of the two")
      solveMatches.foreach { m2 => code = code.replace(m2.group(1).toString, "//solve") }
      
      // extract and remove everything between //test and //endtest
      val test = """[\s\S]*(\/\/test\s*([\s\S]*)\/\/endtest)""".r
      var testCode = ""
      val testMatches = test findFirstMatchIn code
      if (testMatches.isEmpty) throw MatchException("//test / //endtest did not match\n perhaps you are missing one of the two")
      testMatches.foreach { m2 =>
        testCode = m2.group(2).toString
        code = code.replace(m2.group(1).toString, "")
      }
      
      
      map = map + (("codetask" + codetasks, index) -> Map("description" -> description, "code" -> code.toString, "test" -> testCode))
      
      // add one so the next match is processed
      index += 1
    }
  }
  
  // takes an index and counts the
  def parseCurlyBraces(str: String, index: Int): (Int, Int) = {
    var open = 1
    var num = 0
    // count braces
    str.takeRight(str.size - index - 1).foreach({c =>
      if (c == '{' && open > 0) open += 1;
      else if (c == '}' && open > 0) {open -= 1; num += 1}
    })
    
    // get index of closing brace
    var end = index
    for (i <- 0 until num) {
      end = str.indexOf('}', end + 1)
    }
    
    (index, end)
  }
  
  def sort {
    map = ma
  }
  
  def parse: Map[(String, Int), Map[String, String]] = {
    parseVideos
    parseKoans
    parseCodeTasks
    sort
    map
  }
  
  def parseToJson(title: String) {
    parse
    "none"
  }
}

object Parser {
    implicit def string2Parser(s: String) = new Parser(s)
}