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
  //var map = Map[(String, Int), Map[String, String]]()
  var map = TreeMap[Int, (String, Map[String, String])]()
  
  val video = """video\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.+)\")(\s*,?\s*)(\"\"\"(.+)\"\"\"|\"(.+)\")\s*\)""".r
  //val koan = """koan\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.+)\"\s*\)(\s*\{))""".r
  val koan = """koan\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.+)\")(\s*\)\s*\{)""".r
  //val codetask = """codetask\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.*)\"\s*\)(\s*\{))""".r
  val codetask = """codetask\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.*)\")(\s*\)\s*\{)""".r
  val assert = """((should\sequal\s*)|(should\s*===\s*)|(should\s*be\s*)|(shouldEqual\s*)|(shouldBe\s*))((\((.*)\))|((.*)))""".r
  val clean = """^\s*(\S[\s\S]*\S)\s*""".r
  
  def escapeHTML = (s: String) => s.replace("\n", "\\n").replace("\"", "\\\"").replace("\t", "\\t")
  
  def parseVideos {
    val matches = video findAllMatchIn s
    var foundList = List()
    matches.foreach { m =>
      var index = s.indexOf(m.toString)
      
      // prevent overwriting existing map item with same description 
      if (map.contains(index))
        index = s.indexOf(m.toString, index + 1)
      
      videos += 1
      val description = if (m.group(3) != null) m.group(3) else m.group(2).stripMargin('|')
      map = map + (index -> ("video" + videos, Map("description" -> escapeHTML(description), "url" -> escapeHTML(m.group(7)))))
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
      
      // prevent overwriting existing map item with same description 
      if (map.contains(index))
        index = s.indexOf(m.toString, index + 1)
      
      koans += 1
      
      val description = if (m.group(3) != null) m.group(3) else m.group(2).stripMargin('|')
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
      val sol = "[%s]".format(solutions.map(x => "\"" + escapeHTML(x) + "\"").mkString(","))
      map = map + (index -> ("koan" + koans, Map("description" -> escapeHTML(description), "code" -> escapeHTML(code.toString), "solutions" -> sol)))
      
      // add one so the next match is processed
      index += 1
    }
  }
  
  def parseCodeTasks {
    val matches = codetask findAllMatchIn s
    var index = 0
    matches.foreach { m =>
      index = s.indexOf(m.toString, index)
      
      // prevent overwriting existing map item with same description 
      if (map.contains(index))
        index = s.indexOf(m.toString, index + 1)
      
      codetasks += 1
      
      val description = if (m.group(3) != null) m.group(3) else m.group(2).stripMargin('|')
      val pos = parseCurlyBraces(s, index + m.toString.size - 1)
      var code = s.slice(pos._1 + 1, pos._2)
      
      // get tabtype (should have two tabs)
      var tabType = ""
      """\n(\t*| *)\S""".r findFirstMatchIn code foreach { m2 => tabType = m2.group(1).toString }
      // first indentation should be second level
      if (tabType.size % 2 != 0) 
        tabType = ""
      
      // remove whitespace before and after
      clean findFirstMatchIn code foreach { m2 => code = m2.group(1).toString }
      
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
      
      // remove whitespace
      clean findFirstMatchIn code foreach { m2 => code = m2.group(1).toString }
      clean findFirstMatchIn testCode foreach { m2 => testCode = m2.group(1).toString }
      // indentation one level down
      if (code.contains("\t") && testCode.contains("\t")) {
        code = code.replace("\n\t\t", "\n")
        testCode = testCode.replace("\n\t\t", "\n")
      } else {
        code = code.replace("\n    ", "\n")
        testCode = testCode.replace("\n    ", "\n") 
      }
      
      map = map + (index -> ("codetask" + codetasks, Map("description" -> escapeHTML(description), "code" -> escapeHTML(code.toString), "test" -> escapeHTML(testCode))))
      
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
  
  def parse: TreeMap[Int, (String, Map[String, String])] = {
    parseVideos
    parseKoans
    parseCodeTasks
    map
  }
  
  def parseToJson(title: String):String = {
    parse
    val t = "    "
    var json = "{\n%s\"chapter\": {\n%s%s\"title\": \"%s\",\n%s%s\"tasks\": {\n".format(t, t, t, title, t, t)
    // convert map to json string
    map foreach { task =>
      json = json + "%s%s%s\"%s\": {".format(t, t, t, task._2._1)
      task._2._2 foreach { value =>
        if (value._1 != "solutions")
          json = json + "\"%s\": \"%s\",".format(value._1, value._2)
        else
          json = json + "\"%s\": %s,".format(value._1, value._2)
      }
      // strip last ,
      if (json.last == ',') json = json.slice(0, json.size - 1)
      json = json + "},\n"
    }
    // strip last ,\n
    if (json.slice(json.size - 2, json.size) == ",\n") json = json.slice(0, json.size - 2)
    json + "\n%s%s}\n%s}\n}".format(t, t, t)
  }
}

object Parser {
    implicit def string2Parser(s: String) = new Parser(s)
}