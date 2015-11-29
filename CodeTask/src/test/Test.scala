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
    mRegular should equal(Map(("video1", 5) -> Map("description" -> "description()", "url" -> "http://url")))
    mNewlines should equal(Map(("video1", 5) -> Map("description" -> "description()", "url" -> "http://url")))
    mMultiple should equal(Map(("video1", 5) -> Map("description" -> "description()", "url" -> "http://url"),
                               ("video2", 53) -> Map("description" -> "description2()", "url" -> "http://url2")))
  }
  
  "Parser#parse" should "parse koan into map" in {
    val mRegular = Test.koanRegular.parse
    val mNewlines = Test.koanNewlines.parse
    val mRecursive = Test.koanRecursive.parse
    val mMultiple = Test.koanMultiple.parse
    
    mRegular should equal(Map(("koan1", 5) -> Map("description" -> "a koan", "code" -> "1 should be __", "solutions" -> "1")))
    mNewlines should equal(Map(("koan1", 5) -> Map("description" -> "a koan", "code" -> "some code{}\n\"text\" should be __", "solutions" -> "\"text\"")))
    mRecursive should equal(Map(("koan1", 5) -> Map("description" -> "a koan", "code" -> "if(what) {\ndo that\n}\nsome code{}\n1 should be __", "solutions" -> "1")))
    mMultiple should equal(Map(("koan1", 5) -> Map("description" -> "a koan", "code" -> "if(what) {\ndo that\n}\nsome code{}\n1 should be __", "solutions" -> "1"),
                                ("koan2", 85) -> Map("description" -> "a koan", "code" -> "if(what) {\ndo that\n}\nsome code{}\n1 should be __", "solutions" -> "1")))
  }
  
  "Parser#parse" should "parse codetask into map" in {
    val mRegular = Test.codeTaskRegular.parse
    val mMultiple = Test.codeTaskMultiple.parse
    
    mRegular should equal(Map(("codetask1", 5) -> Map("description" -> "a codetask", "code" -> "def x() =>\n  //solve\n", "test" -> "assert(true)\n")))
    mMultiple should equal(Map(("codetask1", 5) -> Map("description" -> "a codetask", "code" -> "def x() =>\n  //solve\n", "test" -> "assert(true)\n"),
                               ("codetask2", 119) -> Map("description" -> "a codetask", "code" -> "def x() =>\n  //solve\n", "test" -> "assert(true)\n")))
  }
  
  "Parser#parse" should "parse text into valid map" in {
    assert(true)
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
  video(""" + "\"\"\"" + """What's an extractor? In Scala it's a method in any `object` called `unapply`, and that method
       | is used to disassemble the object given by returning a tuple wrapped in an option. Extractors can be used
       | to assign values.""" + "\"\"\"" + """, "http://youtube/watch?lpk42")

  koan("das ist ein koan eine aufgabe mit fehlenden assert werten") {
  	result should equal (3) // can customize equality
    result should === (3)   // can customize equality and enforce type constraints
    result should be (3)    // cannot customize equality, so fastest to compile
    result shouldEqual 3    // can customize equality, no parentheses required
    result shouldBe 3       // cannot customize equality, so fastest to compile, no parentheses required
  }
  
  codetask("schreiben sie eine function reverse die eine umgekehrte liste zurück geben") {
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
}"""
}