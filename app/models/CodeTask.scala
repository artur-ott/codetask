package models

import tools.nsc.interpreter.IMain
import tools.nsc.interpreter.Results
import tools.nsc.Settings
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException

class Execution(var error: Boolean = false, var incomplete: Boolean = false, var success: Boolean = false, var consoleOutput: String = "")

class CodeTask(var description: String = "Empty", var code: String = "Empty", var test: String = "Empty") {
	def run(): Execution = {
		val settings = new Settings
		settings.usejavacp.value = true
		val n = new IMain(settings)
		val ex = new Execution
		val out = new java.io.ByteArrayOutputStream

		lazy val f = Future {
			scala.Console.withOut(out) {
				n.interpret(code + "\n" + test) match {
					case Results.Error => ex.error = true;
					case Results.Incomplete => ex.incomplete = true;
					case Results.Success => ex.success = true;
				}
			}
		}

		try {
			Await.result(f, 10 second)
		} catch {
			case e: TimeoutException => ex.incomplete = true
		}

		ex.consoleOutput = out.toString()
		ex
	}
}