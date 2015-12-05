package test

import org.scalatest._
import support.Parser.string2Parser
import support.Parser

class Test extends FlatSpec with Matchers {
  "Parser#parseCurlyBrackets" should "get the right indexes" in {
    (new Parser(""))parseCurlyBraces("sdfsdfsdf{234{6}{}}0", 9) should be((9, 18))
  }
  
  "Parser#parse" should "parse video into map" in {
    val mRegular = Test.videoRegular.parse
    val mNewlines = Test.videoNewlines.parse
    val mMultiple = Test.videoMultiple.parse
    mRegular should equal(Map(5 -> ("video1", Map("description" -> "description()", "url" -> "http://url"))))
    mNewlines should equal(Map(5 -> ("video1", Map("description" -> "description()", "url" -> "http://url"))))
    mMultiple should equal(Map(5 -> ("video1", Map("description" -> "description()", "url" -> "http://url")),
                              53 -> ("video2", Map("description" -> "description2()", "url" -> "http://url2"))))
}
  
  "Parser#parse" should "parse koan into map" in {
    val mRegular = Test.koanRegular.parse
    val mNewlines = Test.koanNewlines.parse
    val mRecursive = Test.koanRecursive.parse
    val mMultiple = Test.koanMultiple.parse
    
    mRegular should equal(Map(5 -> ("koan1", Map("description" -> "a koan", "code" -> "1 should be __", "solutions" -> "1"))))
    mNewlines should equal(Map(5 -> ("koan1", Map("description" -> "a koan", "code" -> "some code{}\\n\\\"text\\\" should be __", "solutions" -> "\\\"text\\\""))))
    mRecursive should equal(Map(5 -> ("koan1", Map("description" -> "a koan", "code" -> "if(what) {\\ndo that\\n}\\nsome code{}\\n1 should be __", "solutions" -> "1"))))
    mMultiple should equal(Map(5 -> ("koan1", Map("description" -> "a koan", "code" -> "if(what) {\\ndo that\\n}\\nsome code{}\\n1 should be __", "solutions" -> "1")),
                                85 -> ("koan2", Map("description" -> "a koan", "code" -> "if(what) {\\ndo that\\n}\\nsome code{}\\n1 should be __", "solutions" -> "1"))))
 }
  
  "Parser#parse" should "parse codetask into map" in {
    val mRegular = Test.codeTaskRegular.parse
    val mMultiple = Test.codeTaskMultiple.parse
    
    mRegular should equal(Map(5 -> ("codetask1", Map("description" -> "a codetask", "code" -> "def x() =>\\n  //solve", "test" -> "assert(true)"))))
    mMultiple should equal(Map(5 -> ("codetask1", Map("description" -> "a codetask", "code" -> "def x() =>\\n  //solve", "test" -> "assert(true)")),
                               119 -> ("codetask2", Map("description" -> "a codetask", "code" -> "def x() =>\\n  //solve", "test" -> "assert(true)"))))
  }
  
  "Parser#parse" should "parse fileText into valid json map" in {
    val json = Test.fileText.parseToJson("AboutTest")
    json should equal(Test.json)
  }
}

object Test {
  val videoRegular = "code\nvideo(\"description()\", \"http://url\")\ncode"
  val videoNewlines = "code\nvideo(\n\"description()\", \n\"http://url\")\ncode"
  val videoMultiple = "code\nvideo(\n\"description()\", \n\"http://url\")\ncodecode\nvideo(\n\"description2()\", \n\"http://url2\")\ncode"
  
  val koanRegular = "code\nkoan(\"a koan\") {\n1 should be 1\n}code\n"
  val koanNewlines = "code\nkoan(\n\"a koan\" \n)  {\n\nsome code{}\n\"text\" should be \"text\"\n}code\n"
  val koanRecursive = "code\nkoan(\n\"a koan\" \n) {\n\nif(what) {\ndo that\n}\nsome code{}\n1 should be 1\n}code\n"
  val koanMultiple = "code\nkoan(\n\"a koan\" \n) {\n\nif(what) {\ndo that\n}\nsome code{}\n1 should be 1\n} code\ncode\nkoan(\n\"a koan\" \n) {\n\nif(what) {\ndo that\n}\nsome code{}\n1 should be 1\n}code\n"
  
  val codeTaskRegular = "code\ncodetask(\"a codetask\")  {\ndef x() =>\n  //solve\n  solve code{}\n  //endsolve\n//test\nassert(true)\n//endtest\n}"
  val codeTaskMultiple = "code\ncodetask(\"a codetask\")  {\ndef x() =>\n  //solve\n  solve code{}\n  //endsolve\n//test\nassert(true)\n//endtest\n} \n code\ncodetask(\"a codetask\")  {\ndef x() =>\n  //solve\n  solve code{}\n  //endsolve\n//test\nassert(true)\n//endtest\n}"
  
  val fileText =
"""package tasks

import support.CodeTaskSuite
import org.scalatest.Matchers

class AboutLists extends CodeTaskSuite {
  video("description", "http://youtube/watch?lpk42")

  koan("das ist ein koan eine aufgabe mit fehlenden assert werten") {
  	result should equal (3)
    result should === (3)
    result should be List(3, 2, 1)
    result shouldEqual "text"
    result shouldBe 3
  }
  
  codetask("schreiben sie eine function reverse die eine umgekehrte liste zurück geben") {
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
}"""
  
  val json =
"""{
    "chapter": {
        "title": "AboutTest",
        "tasks": {
            "video1": {"description": "description","url": "http://youtube/watch?lpk42"},
            "koan1": {"description": "das ist ein koan eine aufgabe mit fehlenden assert werten","code": "result should equal (__)\n    result should === (__)\n    result should be __\n    result shouldEqual __\n    result shouldBe __","solutions": "3;3;List(3, 2, 1);\"text\";3"},
            "codetask1": {"description": "schreiben sie eine function reverse die eine umgekehrte liste zurück geben","code": "def rvrs(l: List[Any]): List[Any] = {\n  //solve\n}","test": "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"}
        }
    }
}"""
}