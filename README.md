#CodeTask

CodeTask ist ein Programm das interaktives Lernen von Scala ermöglicht.
User können videos sehen, koans lösen und eigene codeaufgaben einreichen.
Der Fortschritt der User wird auf dem Server gespeichert.

##Course erstellen

Course basieren auf einem festgelegten format das als *.json* an den Server
gesendet wird. Zur erstellung von Coursen steht ein Hilfsscript *codetask*
bereit. Das Script wird folgendermaßen benutzt:

```
help                                                     | show help
create course "Course Title" /path/to/scala/tests        | create course
update course "Course Title" /path/to/scala/tests        | update course
delete course "Course Title"                             | delete course
parse "Course Title" /path/to/scala/tests ./to/file.json | create json file of course
create course /path/to/file.json                         | create course from json file
update course /path/to/file.json                         | update course from json file
```
Das Script übersetzt mit *create* und *update* die *.scala* Dateien von
`CodeTaskSuite` Klassen in eine *.json* Datei und sendet diese an den Server.
Einzelne `CodeTaskSuite` Klassen bilden die Chapter eines Courses. Beispiel:

```
/folder
    /scala1course
        ChapterOne.scala
        ChapterTwo.scala
        ChapterThree.scala
```
Die oben beschriebene Ordnerstruktur würde in einen Course mit drei Chaptern
übersetzt.

##CodeTaskSuite

Im Ordner `/CodeTask` befindet sich ein Eclipse Projekt das die `CodeTaskSuite`
Klasse beinhaltet. Diese erbt von `org.scalatest.FunSuite` und kann als test
ausgeführt werden. Außerdem verfügt sie über die Methoden `video`, `koan` und
`codetask` mit denen die Aufgaben der einzelenen Chaptern definiert werden.
Eine Suite könnte zum Beispiel so aussehen:

```scala
// Inhalt von AboutLists.scala
package tasks

import support.CodeTaskSuite
import org.scalatest.Matchers

// CodeTaskSuites werden als chapters in courses hinzugefügt
class AboutLists extends CodeTaskSuite {
  // keine tabs und indentation 2 space oder nur tabs bei multiline kommentaren
  video("Viedeo zu Scala-Listen.", "U23j6yH21W4")

  koan("""Mit der Funktion <b>contains</b> kann geprüft werden ob eine Liste ein bestimmtes Element enthält.
        | Mit der Funktion <b>map</b> können funktionen auf listen angewendet werden, die
        | Ergebnisse werden in einer neuen Liste gespeichert.
        | Versuch in dem folgenden <b>Koan</b> die richtigen Werte einzutragen""") {
    val l = List(1, 2, 3, 4)
    val l2 = l.map { x => x + 1 }
    val l3 = l.map { x => x * x }

    l should be (List(1, 2, 3, 4))
    l2 should be(List(2, 3, 4, 5))
    l3 shouldBe List(1, 4, 9, 16)
  }

  koan("Zu Listen können auch Werte hinzugefügt werden.<br>Dies kann mit <b>++</b> geschehen.") {
    val l = List(1, 3, 5)
    val l2 = l ++ List(6)

    l2 shouldBe List(1, 3, 5, 6)
  }

  codetask("""schreiben sie eine function reverse die eine umgekehrte liste zurück geben.
            | Nutzen Sie nicht die bereits vorhandenen Möglichkeit
            | <b>List.reverse</b>""") {
    def rvrs(l: List[Any]): List[Any] = {
      //solve
      l match {
        case h :: tail => rvrs(tail) ::: List(h)
        case _       => Nil
      }
      //endsolve
    }

    //test
    rvrs(List(1, 2, 3)) should be(List(3, 2, 1))
    //endtest
  }  
}
```
###Videos

Videos werden mit der Methode `video` definiert und haben eine Description und
eine Youtube-URL oder den Video-Key.

###Koans

Koans werden mit der `koan` Methode definiert. Koans sind Code-Lückentexte bei
denen die Assertion vom User eingegeben werden
muss. Für die folgenden Assertions werden Ergebnise ersetzt:

```
result should equal (x)   // -> result should equal (__)
result should === (x)     // -> result should === (__)
result should be (x)      // -> result should be (__)
result shouldEqual x      // -> result shouldEqual __
result shouldBe x         // -> result shouldBe __
```

###Codetask

Mit der Methode `codetask` werden Codetasks definiert.
Codetasks sind Aufgaben bei denen der User einen Code schreiben muss der
bestimmte assertions erfüllen muss. Die Aufgabe kann formuliert werden so dass
der Test ausführbar is. Bei der umwandlung in *.json* wird die Lösung die
zwischen einzeiligen `//solve` und `//endsolve` Kommentaren geschrieben wird,
aus dem code herausgenommen.
Die Assertions die zutreffen sollen werden zwischen einzeilige `//test` und
`//endtest` Kommentare geschrieben.

## Server Starten

Um den Server zu Starten:

- compilieren mit ./activator dist
- compilierte zip entpacken in /target/universal
- application datei in /target/universal/entpackt/ ausführbar machen mit chmod +x codetask
- ./codetask -Dhttp.port=9000 -Dplay.crypto.secret="19;Yb1AVDOWOcxmYqmv^F5M?<=dqt;I<ApsTX5=[1FmDRD5X0ZYb<8qECZYFhRQR" &

falls bereits ein server auf dem port 9000 läuft kann dieser mit lsof -i -P | grep "9000" ausfindig und mit kill gestoppt werden.
