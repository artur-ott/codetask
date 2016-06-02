package models

import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent._
import scala.collection.immutable.TreeMap
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import scala.collection.mutable.ListBuffer
import models.Course._
import models.Chapter

object CourseParser {
    val GHAPI = "https://api.github.com/"

    def mkGithubApiUrl(githubUser: String, githubRepo: String, path: String): 
      String = {
        GHAPI + "repos/" + githubUser + "/" + githubRepo + 
          "/contents" + path
    }

    @throws[Exception]
    @throws[TimeoutException]
    def parseFromGithub(url: String, title: String): Course = {

        // get repository path contents
        lazy val f = WS.url(url).get()
        val result = Await.result(f, 10.second)

        if (result.status != 200) {
          play.Logger.info("github path not found code: " + result.status)
          throw new Exception("github path not found http code: " 
            + result.status)
        }

        var chapters = List[(Chapter, Int)]()
        var chapterStrings = List[(String, String)]()
        val json = result.json
        
        val contentDownloadUrls = json.as[List[JsValue]].map { js =>
          val raw = (js \ "download_url").get.toString
          // remove quotes ""
          raw.slice(1, raw.size - 1)
        }.filter(_.endsWith(".scala"))

        for (downloadUrl <- contentDownloadUrls) {
          lazy val f2 = WS.url(downloadUrl).get()
          val result2 = Await.result(f2, 10.seconds)

          val reg = """\/(\w*)\.scala$""".r
          val matched = reg findFirstMatchIn downloadUrl
          var fileTitle = matched match {
            case Some(m) => m.group(1)
            case None => "No Title"
          }

          if (result.status == 200) {
            chapterStrings = chapterStrings ::: List((result2.body, fileTitle))
          } else {
            play.Logger.info("github path not found code: " + result.status)
          }
        }

        // parse all contents of .scala files to chapters
        var i = 0
        chapters = for (cs <- chapterStrings) yield {
          val c = parseChapter(cs._1, cs._2, i)
          i = i + 1
          (c._1, c._2)
        }

        chapters = chapters.sortWith(_._2 < _._2)
        val tmpChapters = chapters.map(_._1)

        Course(-1, title, tmpChapters, Some(url))
    }

    def parseFromFiles(files: Array[java.io.File], title: String): Course = {
      val chapters = files.map { file =>
        val s = scala.io.Source.fromFile(file).getLines mkString "\n"
        parseChapter(s, "none", -1)
      }

      val sortedChapters = chapters.sortWith(_._2 < _._2)
      val onlyChapters = chapters.map(_._1).toList
      Course(-1, title, onlyChapters, None)
    }

    def parseChapter(chapterString: String, title: String, id: Long): (Chapter, Int) = {
      val result = (new Parser(chapterString)).parseChapter(title, id)
      val json = Json.parse(result._1)
      val chapter = json.validate[Chapter].get
      (chapter, result._2)
    }
}

case class Node(key: String, value: Option[String] = None, values: Option[List[Node]] = None) {
  override def toString = value match {
    case Some(v) => "\"%s\":%s".format(key, v)
    case None => values match {
      case Some(vs) => "\"%s\":{%s}".format(key, vs.map(_.toString).mkString(","))
      case None => throw new Exception("missing value / values for" + key)
    }
  }
}

// helper class
case class MatchException(smth:String)  extends Exception(smth)

class Parser(s: String) {
  var videoCount = 0
  var koanCount = 0
  var codetaskCount = 0
  var taskMap = TreeMap[Int, List[Node]]()
  var title:Option[String] = None
  var rank:Option[Int] = None

  def escapeHTML = (s: String) => s
    .replace("\\", "\\\\")
    .replace("\n", "\\n")
    .replace("\"", "\\\"")
    .replace("\t", "\\t")
  def inString = (s: String) => "\"" + s + "\""

  val video = """video\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.+)\")(\s*,?\s*)(\"\"\"(.+)\"\"\"|\"(.+)\")\s*\)""".r
  val koan = """koan\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.+)\")(\s*\)\s*\{)""".r
  val codetask = """codetask\s*\(\s*(\"\"\"([\s\S]*?)\"\"\"|\"(.*)\")(\s*\)\s*\{)""".r

  val info = """CodeTaskSuite\s*\(\s*\"(.*)\"\s*,\s*([1-9]*)\)""".r

  // regex, solution group nummer, replacement function
  val assertList = List[(Regex, Int, Match => String)](
    ("(should\\s+((equal)|(==)|(===)|(be)|(eq))\\s*)(\\((.*)\\))".r, 9, {m => m.group(1).toString + "(__)"}),
    ("(should\\s+((equal)|(==)|(===)|(be)|(eq))\\s+)(.*)".r,         8, {m => m.group(1).toString + "__"}),
    ("(should((Equal)|(Be))\\s+)(.*)".r,                             5, {m => m.group(1).toString + "__"}),
    ("(should((Equal)|(Be))\\s+)(\\((.*)\\))".r,                     6, {m => m.group(1).toString + "(__)"}),
    ("(assert\\s*\\(.*((==)|(===)|(eq))\\s+)(.*)(,.*)".r,            6, {m => m.group(1).toString + "__" + m.group(7).toString}),
    ("(assert\\s*\\(.*((==)|(===)|(eq))\\s+)(.*)\\)".r,              6, {m => m.group(1).toString + "__)"})
  )
  val clean = """^\s*(\S[\s\S]*\S)\s*""".r

  def parseInfo {
    val matched = info findFirstMatchIn s
    matched match {
      case Some(m) => title = Some(m.group(1))
                      rank  = Some(m.group(2).toInt)
      case None =>
    }
  }

  def parseVideos {
    val matches = video findAllMatchIn s
    matches.foreach { m =>
      var index = s.indexOf(m.toString)

      // prevent overwriting existing map item with same description
      if (taskMap.contains(index))
        index = s.indexOf(m.toString, index + 1)

      val description = if (m.group(3) != null) m.group(3) else m.group(2).stripMargin('|')
      val url = m.group(7)
      videoCount += 1

      taskMap = taskMap + (index -> List(
        Node("id",       Some(inString("video" + videoCount))),
        Node("tag",      Some(inString("video-task"))),
        Node("data",     None, Some(List(
          Node("description", Some(inString(escapeHTML(description)))),
          Node("url",         Some(inString(escapeHTML(url))))
        ))),
        Node("solution", Some(inString("watched")))
      ))
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
      if (taskMap.contains(index))
        index = s.indexOf(m.toString, index + 1)

      koanCount += 1

      val description = if (m.group(3) != null) m.group(3) else m.group(2).stripMargin('|')
      val pos = parseCurlyBraces(s, index + m.toString.size - 1)

      // get code and remove whitespace before and after
      var code = ""
      //clean findFirstMatchIn (s.slice(pos._1 + 1, pos._2)) foreach { m2 => code = m2.group(1).toString }
      code = s.slice(pos._1 + 1, pos._2)
      // retrieve all solutions and replace them with __
      var solutions = List[String]()

      /*assert findAllMatchIn code foreach { m2 =>
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
      }*/
     //assertList.foreach { assert =>
      //  assert._1 findAllMatchIn code foreach { am =>
      //    solutions = solutions ::: List(am.group(assert._2))
      //    code = code.replace(am.toString, assert._3(am))
      //  }
      //}
      code.split("\n").foreach { line =>
        var end = false
        var i = 0
        do {
          val assert = assertList(i)
          assert._1 findFirstMatchIn line match {
            case Some(am) => {
              solutions = solutions ::: List(am.group(assert._2))
              code = code.replace(am.toString, assert._3(am))
              end = true
            }
            case None =>
          }
          i = i + 1
        } while(i < assertList.length && !end)
      }

      val solutionWithoutBrackets = solutions.map(x => escapeHTML(x)).mkString(",")
      val solutionWithBrackets = "[%s]".format(solutions.map(x => "\"" + escapeHTML(x) + "\"").mkString(","))

      //println(code)
      //println("---------------------------------------------------------")
      ////val indent = "[ |\t]*".r findFirstMatchIn code
      //println("indent:<" + indent + ">")
      //indent match {
      //  case Some(s) => {
      //    // remove first indentation in each line
      //    code = code.split("\n").map(_.replaceFirst(s.toString, "")).mkString("\n")
      //    println("---------------------------------------------------------")
      //  }
      //  case None =>
      //}

      //remove empty Lines before and after code
      val empty = "^[ |\t]*$".r
      val lines = code.split("\n")
      var filtered = new ListBuffer[String]()
      var i = 0
      var begin = false
      do {
        if (begin || ((empty findFirstMatchIn lines(i)).isEmpty)) {
          filtered += lines(i)
          begin = true
        }
        i = i + 1
      } while(i < lines.length)

      val newLines = filtered.toList
      filtered = new ListBuffer[String]()
      i = newLines.length - 1
      begin = false
      do {
        if (begin || ((empty findFirstMatchIn newLines(i)).isEmpty)) {
          filtered += newLines(i)
          begin = true
        }
        i = i - 1
      } while(i >= 0)
      val cleanLines = filtered.toList.reverse

      // bringdown indentation
      val indent = "^([ |\t]*)\\S".r
      val indentMatch = indent findFirstMatchIn cleanLines(0)
      val space = indentMatch match {
        case Some(found) => found.group(1)
        case None => ""
      }
      code = cleanLines.map(_.replaceFirst(space, "")).mkString("\n")

      taskMap = taskMap + (index -> List(
        Node("id",       Some(inString("koan" + koanCount))),
        Node("tag",      Some(inString("koan-task"))),
        Node("data",     None, Some(List(
          Node("description", Some(inString(escapeHTML(description)))),
          Node("code",        Some(inString(escapeHTML(code.toString)))),
          Node("mode",        Some(inString("scala"))),
          Node("solutions",   Some(solutionWithBrackets))
        ))),
        Node("solution", Some(inString(solutionWithoutBrackets)))
      ))

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
      if (taskMap.contains(index))
        index = s.indexOf(m.toString, index + 1)

      codetaskCount += 1

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
      val solve = """[\s]*(\/\/solve\s*[\s\S]*?\/\/endsolve)""".r
      val solveMatches = solve findAllMatchIn code
      if (solveMatches.isEmpty) throw MatchException("//solve / //endsolve did not match\n perhaps you are missing one of the two")
      solveMatches.foreach { m2 => code = code.replace(m2.group(1).toString, "//todo") }

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

      //println(code)
      //indentation one level down
      if (code.contains("\t") && testCode.contains("\t")) {
        code = code.replace("\n\t\t", "\n")
        testCode = testCode.replace("\n\t\t", "\n")
      } else {
        code = code.replace("\n    ", "\n")
        testCode = testCode.replace("\n    ", "\n")
      }

      taskMap = taskMap + (index -> List(
        Node("id",       Some(inString("code" + codetaskCount))),
        Node("tag",      Some(inString("code-task"))),
        Node("data",     None, Some(List(
          Node("description", Some(inString(escapeHTML(description)))),
          Node("code",        Some(inString(escapeHTML(code.toString)))),
          Node("mode",        Some(inString(escapeHTML("scala"))))
        ))),
        Node("solution", Some(inString(escapeHTML(testCode))))
      ))

      // add one so the next match is processed
      index += 1
    }
  }

  // takes an index and counts the
  def parseCurlyBraces(str: String, index: Int): (Int, Int) = {
    var open = 1
    var num = 0

    // count braces
    str.takeRight(str.size - index - 1).foreach { char =>
      if (char == '{' && open > 0) open += 1;
      else if (char == '}' && open > 0) {open -= 1; num += 1}
    }

    // get index of closing brace
    var end = index
    for (i <- 0 until num) {
      end = str.indexOf('}', end + 1)
    }

    (index, end)
  }

  def parseChapter(chapterTitle: String, id: Long = 1): (String, Int) = {
    parseVideos
    parseKoans
    parseCodeTasks
    parseInfo

    var tasks = taskMap.map{ x => "{%s}".format(x._2.map(_.toString()).mkString(","))}
    var taskArray = "[%s]".format(tasks.mkString(","))
    
    val tmpTitle = title match {
      case Some(t) => t
      case None => chapterTitle
    }

    val tmpRank = rank match {
      case Some(r) => r
      case None => 0
    }

    play.Logger.info(tmpTitle + ", " + tmpRank)

    var chapter = "{\"id\": %d, \"title\": \"%s\", \"tasks\": %s}".format(tmpRank, tmpTitle, taskArray);
    (chapter, tmpRank)
  }
}
