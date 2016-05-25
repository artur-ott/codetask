import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models._
import models.Services.userService

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class InterpreterSpec extends Specification {
  "Interpreter" should {
    "run code successful" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n  l.reverse\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual true
    }
    "run code unsuccessful" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n  l\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "interrupt infinite loop" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n  while(true) {}\nl\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.incomplete shouldEqual true
    }
    "fail with infinite recoursion" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n rvrs(l)\n l\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with System.exit(1)" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nSystem.exit(1)"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "import math successful" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n  l.reverse\n}"
      val test = "import scala.math._ \nrvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual true
    }
    "fail with use of java.io.File" in {
      val code = "import java.io.File\n import scala.io.Source._\nprintln((new File(\"./\")).listFiles.filter(_.isFile).toList)\ndef rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with use of java.net.Http" in {
      val code = "val url = java.net.URL(\"http://localhost\")\ndef rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with invalid import play.api.Logger" in {
      val code = "import play.api.Logger\ndef rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\n Logger.info(\"test\") "
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with invalid use of library tools" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nval x = new tools.nsc.interpreter.IMain()"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with invalid use of library tools" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nval x = new scala.tools.nsc.interpreter.IMain()"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
    "fail with ascii encoded use of library tools" in {
      val code = "def rvrs(l: List[Any]): List[Any] = {\n l.reverse\n}\nval x = new \u0074ools.nsc.interpreter.IMain()"
      val test = "rvrs(List(1, 2, 3)) should be(List(3, 2, 1))"
      val result = Interpreter.run("scala", code + "\n" + test)
      result.success shouldEqual false
      result.error shouldEqual true
    }
  }
}
