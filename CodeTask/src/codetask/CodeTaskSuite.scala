package codetask

import org.scalatest.{FunSuite}
import org.scalatest.Matchers

class CodeTaskSuite extends FunSuite with Matchers {
  def video(description: String, url: String) = Unit
  def koan(name: String)(f: => Unit) { test(name.stripMargin('|'))(f) }
  def codetask(name:String)(f: => Unit) { test(name.stripMargin('|'))(f) }
}