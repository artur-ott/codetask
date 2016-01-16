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
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

case class InterpreterResult(
  val invalid: Boolean = false,
  var error: Boolean = false,
  var incomplete: Boolean = false,
  var success: Boolean = false,
  var output: String = ""
)

//class WhiteListClassLoader(cl: java.lang.ClassLoader) extends java.lang.ClassLoader {
//  val whiteList = List("Math")
//
//  //override def loadClass(name: String): Class[_] = {
//  //  println(name)
//  //  //val b = cl.loadClassData(name);
//  //  //return defineClass(name, b, 0, b.length);
//  //  cl.loadClass(name)
//  //}
//
//  override def findClass(name:String): Class[_] = {
//    println(name)
//    cl.findClass(name)
//  }
//
//  //override def 
//
//
// // override def clearAssertionStatus() = cl.clearAssertionStatus()
// // //override def getParent() = cl.getParent()
// // override def getResource(name:String) = cl.getResource(name)
// // override def getResourceAsStream(name:String) = cl.getResourceAsStream(name)
// // override def getResources(name:String) = cl.getResources(name)
// // override def setClassAssertionStatus(className: String, enabled: Boolean) = cl.setClassAssertionStatus(className, enabled)
// // override def setDefaultAssertionStatus(enabled: Boolean) = cl.setDefaultAssertionStatus(enabled)
// // override def setPackageAssertionStatus(packageName: String, enabled: Boolean) = cl.setPackageAssertionStatus(packageName, enabled)
//  def loadClassData(name: String): Array[Byte] = {
//    println(name)
//    return super.loadClassData(name)
//  }
//}

/*
class WhiteListClassLoader extends scala.tools.nsc.util.ScalaClassLoader.URLClassLoader {
  def findClass(name: String): Array[Byte] = {
    val b = loadClassData(name);
    return defineClass(name, b, 0, b.length);
  }

  def loadClassData(name: String): Array[Byte] = {

  }

  def getPermissions(codeSource: CodeSource) {
    val permissions = new java.security.PermissionsCollection()

    val addRead = () => 
      permissions.add(new java.io.FilePermission(codeSource.url, "read"))

    println(codeSource.url)
    codeSource.url match {
      case "lib/scala-library.jar" => addRead()
      case "lib/scalatest.jar" => addRead()
      case _ => // no permissions
    }
  }
}*/

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
    //println(settings.classpath.value)
    settings.classpath.value += File.pathSeparator + "lib/scalatest.jar"
    //println(settings.classpath.value)

    //val classLoader = new java.net.URLClassLoader("lib/scala-library.jar", null)
    val url = (jar: String) => (new java.io.File(jar)).toURI.toURL
    val x = settings.getClass.getClassLoader
    //val whiteListClassLoader = new WhiteListClassLoader(settings.getClass.getClassLoader())/*new WhiteListClassLoader(Seq(
      //url("lib/scala-library.jar"),
      //url("lib/scalatest.jar")), null)*/
    
    val im = new IMain(settings) {
      override protected def parentClassLoader = settings.getClass.getClassLoader()
      //override protected def parentClassLoader = whiteListClassLoader
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