package test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.codetask.koanlib.CodeTaskSuite


@RunWith(classOf[JUnitRunner])
class Sets extends CodeTaskSuite("title", 1) {
  koan("""Sets sind Collections die jedes Element maximal einmal beinhalten.
        | Damit stellen Sets Mengen dar.""") {
    val M = Set(1, 2, 3)
  }

  koan("""Sets haben eine Methode <b>contains</b> die überprüft ob ein Element im Set enthalten ist.
        | Ist das Element enthalten so wird <b>true</b> zurückgegeben.
        | Falls das Element nicht enthalten is wird <b>false</b> zurückgegeben.""") {
    val M = Set(1, 2, 3, 4)
    M.contains(3) should be(true)
  }

  koan("""Die Mathematische Aussagen:<br>
        | M = {4, 5, 6} <br>
        | 4 &isin; M <br>
        | 2 &notin; M <br>
        | können in Scala folgenderweise ausgedrückt werden.""") {
    val M = Set(4, 5, 6)
    M.contains(4) should be(true)
    M.contains(2) should be(false)
  }

  koan("""Sets können mit dem <b>==</b> Operator überprüft werden.
        | Die Reihenfolge der Elemente Spielt keine Rolle. Mathematisch: M1 = M2.""") {
    Set(1, 2, 3) == Set(3, 2, 1) should be(true)
    Set(1, 5) == Set(1, 5, 7) should be(false)
  }

  koan("""Ob Sets nicht gleich sind kann mit dem <b>!=</b> Operator überprüft werden.
        | Mathematisch: M1 &ne; M2.""") {
    Set(4, 5, 6) != Set(1, 2, 3) should be(true)
  }

  koan("""Die Kardinalität m = |M| entspricht der Anzahl von Elementen in einem Set bzw. einer Menge.
        | Die Anzahl der Elemente in einem Set kann über die <b>size</b> Methode ausgegeben werden.""") {
    Set().size should equal(0)
    Set(1, 2, 3).size should equal(3)
  }

  koan("""Eine Teilmenge M1 &sube; M2 kann mit der <b>subsetOf</b> festgestellt werden,
        | die einen Boolean Wert zurückgibt""") {
    val M1 = Set(1, 2)
    val M2 = Set(1, 2, 3, 4)

    M1.subsetOf(M2) should be(true)
  }

  koan("""Um festzustellen ob es sich um eine Echte Teilmenge handelt,
        | muss gelten M1 &isnot; M2""") {
    val M1 = Set(1, 2)
    val M2 = Set(1, 2, 3, 4)

    M1.subsetOf(M2) should be(true)
    M1 != M2 should be(true)
  }

  koan("""Scala bietet die Möglichkeit Sets mit Methoden zu verknüpfen.<br>
        | <table>
        | <tr><th>Mathematisch</th><th>Scala</th><th>Beschreibung</th></tr>
        | <tr><td>M1 &cup; M2</td><td>M1 ++ M2</td><td>Vereinigung</td></tr>
        | <tr><td>M1 &cap; M2</td><td>M1 & M2</td><td>Schnitt</td></tr>
        | <tr><td>M1 \ M2</td><td>M1 &~ M2</td><td>Differenz</td></tr>
        | </table>
       """) {
    Set(1, 2, 3) ++ Set(4, 5, 6) == Set(1, 2, 3, 4, 5, 6) should be(true)
    Set(1, 2) ++ Set(1, 5) == Set(1, 2, 5) should be(true)
    
    //Set(1, 2, 3) & Set(1, 2, 4) == Set(1, 2) should be(true)
    //Set(5) & Set(7, 8) == Set() should be(true)
    //Set(1, 2, 3, 4) &~ Set(3, 4) == Set(1, 2) should be(true)
    //Set(1, 2) &~ Set(1, 2) == Set() should be(true)
  }

  // mehr codetasks
}