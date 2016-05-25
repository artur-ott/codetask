package models


import org.scalatest.Matchers._
import tools.nsc.interpreter.IMain
import tools.nsc.interpreter.Results
import tools.nsc._
import tools.nsc.Settings
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException
import java.io.File
import java.net.URL;
import java.net.URLClassLoader
import java.lang.SecurityManager
import java.security.Permission
import java.lang.SecurityException

case class InterpreterResult(
  var error: Boolean = false,
  var incomplete: Boolean = false,
  var success: Boolean = false,
  var output: String = ""
)

// borrowed: https://github.com/wsargent/sandboxexperiment/blob/master/security/src/main/scala/com/tersesystems/sandboxexperiment/security/SandboxClassLoader.scala
class SecureClassLoader(parent: ClassLoader) extends URLClassLoader(SecureClassLoader.urls, parent) {

  import SecureClassLoader._

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if (!isAllowed(name)) {
      throw new IllegalArgumentException("This functionality is disabled")
    }
    super.loadClass(name, resolve)
  }

  override def findClass(name: String): Class[_] = {
    super.findClass(name)
  }
}

object SecureClassLoader {
  val urls = Array(new URL("file://lib/scala-library.jar"), 
                   new URL("file://lib/scalatest.jar"),
                   new URL("file://lib/spire_2.11-0.11.0.jar"))

  val miscClasses =
    """java.util.logging.Logger
      |java.sql.DriverManager
      |javax.sql.rowset.serial.SerialJavaObject
    """.stripMargin.split("\n").toSet

  // a bit extreme, but see http://www.security-explorations.com/materials/se-2014-02-report.pdf
  val javaClasses =
    """java.lang.Class
      |java.lang.ClassLoader
      |java.lang.Package
      |java.lang.invoke.MethodHandleProxies
      |java.lang.reflect.Proxy
      |java.lang.reflect.Constructor
      |java.lang.reflect.Method
      |java.lang.SecurityManager
    """.stripMargin.split("\n").toSet

  val forbiddenPackages =
    """scala.io
      |scala.tools
      |java.io
      |java.net
      |java.sql
      |play
    """.stripMargin.split("\n").toSet

  val forbiddenClasses: Set[String] = javaClasses ++ miscClasses

  def isAllowed(name: String): Boolean = {
    !forbiddenClasses.contains(name) && forbiddenPackages.find(p => name.contains(p)).isEmpty
  }
}

object Interpreter {
  val stdImportScala = "import org.scalatest.Matchers._\n"

  def run(language: String, code: String) : InterpreterResult = {
    language match {
      case "scala" => runScala(code)
    }
  }

  def runScala(code: String): InterpreterResult = {

    val settings = new Settings
    settings.usejavacp.value = true

    // borrowed: http://stackoverflow.com/questions/16511233/scala-tools-nsc-imain-within-play-2-1
    settings.classpath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library.jar"
    settings.classpath.value += File.pathSeparator + "lib/scalatest.jar"
    settings.classpath.value += File.pathSeparator + "lib/spire_2.11-0.11.0.jar"

    val im = new IMain(settings) {
      // SecureClassLoader needs to be created in parentClassLoader
      override protected def parentClassLoader = 
        new SecureClassLoader(settings.getClass.getClassLoader())
    }

    val ir = new InterpreterResult
    val out = new java.io.ByteArrayOutputStream
    // borrowed http://stackoverflow.com/questions/5401281/preventing-system-exit-from-api
    val sm = new SecurityManager() {
      override def checkPermission(permission: Permission) = {
        if (permission.getName().contains("exitVM")) {
          throw new SecurityException()
        }
      }
    }

    lazy val f = Future {
      scala.Console.withOut(out) {
        // add stdImport
        val newCode = stdImportScala + code

        // set security manager
        System.setSecurityManager(sm)

        im.interpret(newCode) match {
          case Results.Error => ir.error = true;
          case Results.Incomplete => ir.incomplete = true;
          case Results.Success => ir.success = true;
        }

        // reset security manager
        System.setSecurityManager(null)

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