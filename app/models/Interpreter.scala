package models

import org.scalatest.Matchers._
import tools.nsc.interpreter.IMain
import tools.nsc.interpreter.Results
import tools.nsc._
import tools.nsc.Settings
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException
import java.io.File

class InterpreterResult(
  var error: Boolean = false,
  var incomplete: Boolean = false,
  var success: Boolean = false,
  var output: String = ""
)

object Interpreter {
  val stdImport = "import org.scalatest.Matchers._\n"

  def run(language: String, code: String) : InterpreterResult = {
    language match {
      case "scala" => runScala(code)
    }
  }

  def runScala(code: String): InterpreterResult = {
    val settings = new Settings
    //settings.embeddedDefaults(org.scalatest.Matchers.getClass().getClassLoader())


    // borrowed: http://stackoverflow.com/questions/16511233/scala-tools-nsc-imain-within-play-2-1
    //settings.bootclasspath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library.jar"
    settings.classpath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library.jar"
    settings.classpath.value += File.pathSeparator + "lib/scalatest.jar"
    
    val im = new IMain(settings){
      override protected def parentClassLoader = this.getClass().getClassLoader()
    }
    // /borrowed

    val ir = new InterpreterResult
    val out = new java.io.ByteArrayOutputStream

    lazy val f = Future {
      scala.Console.withOut(out) {
        // add stdImport
        val newCode = stdImport + code
        im.interpret(newCode) match {
          case Results.Error => ir.error = true;
          case Results.Incomplete => ir.incomplete = true;
          case Results.Success => ir.success = true;
        }
      }
    }

    try {
      Await.result(f, 10 second)
    } catch {
      case e: TimeoutException => ir.incomplete = true
    }

    ir.output = out.toString().replaceFirst(stdImport, "")
    ir
  }
}