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
import java.lang.ClassLoader

case class InterpreterResult(
  val invalid: Boolean = false,
  var error: Boolean = false,
  var incomplete: Boolean = false,
  var success: Boolean = false,
  var output: String = ""
)

//class MyClassLoader extends ClassLoader {
//  def Class[Any] loadClass
//}

object Interpreter {
  val stdImportScala = "import org.scalatest.Matchers._\n"
  val blacklistScala = List("scala", "annotation", "beans", "compat", "io", 
    "ref", "reflect", "runtime", "sys", "text", "System", "java")

  def run(language: String, code: String) : InterpreterResult = {
    language match {
      case "scala" => runScala(code)
    }
  }

  def runScala(code: String): InterpreterResult = {

    val settings = new Settings
    settings.usejavacp.value = false

    // borrowed: http://stackoverflow.com/questions/16511233/scala-tools-nsc-imain-within-play-2-1
    //settings.bootclasspath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library.jar"
    settings.classpath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library.jar"
    println(settings.classpath.value)
    settings.classpath.value += File.pathSeparator + "lib/scalatest.jar"
    println(settings.classpath.value)

    val classLoader = new java.net.URLClassLoader("lib/scala-library.jar", null)
    
    val im = new IMain(settings) {
      //override protected def parentClassLoader = settings.getClass.getClassLoader()
      override protected def parentClassLoader = classLoader
    }
    // /borrowed

    val ir = new InterpreterResult
    val out = new java.io.ByteArrayOutputStream

    lazy val f = Future {
      scala.Console.withOut(out) {
        // add stdImport
        val newCode = stdImportScala + code
        im.interpret(newCode) match {
          case Results.Error => ir.error = true;
          case Results.Incomplete => ir.incomplete = true;
          case Results.Success => ir.success = true;
        }
        //im.close()
      }
    }

    try {
      Await.result(f, 10 second)
    } catch {
      case e: TimeoutException => ir.incomplete = true
    }

    ir.output = out.toString().replaceFirst(stdImportScala, "")
    ir
  }
}