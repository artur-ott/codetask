package models

//import com.twitter.util.Eval
import tools.nsc.interpreter.IMain
import tools.nsc.interpreter.Results
import tools.nsc.Settings

class Execution(val error: Boolean = false, val incomplete: Boolean = false, val success: Boolean = false, val consoleOutput: String = "")

class CodeTask(var description: String = "Empty", var code: String = "Empty", var test: String = "Empty") {
	def run(): Execution = {
		val settings = new Settings
		settings.usejavacp.value = true
		settings.noCompletion.value = false
		val n = new IMain(settings)
		val ex = new Execution

		n.interpret(code + "\n" + test) match {
			case Results.Error => new Execution(error = true)
			case Results.Incomplete => new Execution(incomplete = true)
			case Results.Success => new Execution(success = true)
		}
	}
}