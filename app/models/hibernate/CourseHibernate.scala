package models.hibernate

import models.tasks._
import models._
import javax.persistence._
import org.hibernate.annotations.GenericGenerator
import scala.collection.JavaConverters._
import models.tasks.Tasks._
import play.api.libs.json._

@Entity
@Table(name = "CourseHibernate")
class CourseHibernate extends Serializable {
  @Id
  @GenericGenerator(name="generator", strategy="increment")
  @GeneratedValue(generator="generator")
  var id: Long = _
  var title: String = _

  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "courseH", orphanRemoval = true)
  var chapters: java.util.List[ChapterHibernate] = _

  def fill(course: Course): CourseHibernate = {
    //id = course.id 
    title = course.title

    val css = course.chapters.map{x => (new ChapterHibernate).fill(x, this)}.asJava
    chapters match {
      case null => chapters = css
      case _ => chapters.clear(); chapters.addAll(css)
    }

    this
  }

  def toCourse: Course = {
    Course(id, title, chapters.asScala.map{x => x.toChapter}.toList)
  }
}

@Entity
@Table(name = "ChapterHibernate")
class ChapterHibernate extends Serializable {
  @Id
  @GenericGenerator(name="generator", strategy="increment")
  @GeneratedValue(generator="generator")
  var idH: Long =_

  var id: Long = _
  var title: String = _

  @ManyToOne
  var courseH: CourseHibernate = _

  //@ElementCollection(targetClass = classOf[TaskHibernate])
  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "chapterH", orphanRemoval = true)
  var tasks: java.util.List[TaskHibernate] = _

  def fill(cs: Chapter, uh: CourseHibernate): ChapterHibernate = {
    courseH = uh
    id = cs.id
    title = cs.title

    val sts = cs.tasks.map{x => (new TaskHibernate).fill(x, this)}.toList.asJava
    tasks match {
      case null => tasks = sts
      case _ => tasks.clear(); tasks.addAll(sts)
    }
    this
  }

  def toChapter: Chapter = {
    Chapter(id, title, tasks.asScala.map{x => x.toTask}.toList)
  }
}

@Entity
@Table(name = "TaskHibernate")
class TaskHibernate extends Serializable {
  @Id
  @GenericGenerator(name="generator", strategy="increment")
  @GeneratedValue(generator="generator")
  var idH: Long =_

  var id: String =_
  var tag: String = _
  var taskData: String = _
  var solution: Option[String] = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  var chapterH: ChapterHibernate = _

  def fill(ts: Task, csh: ChapterHibernate): TaskHibernate = {
    chapterH = csh
    id = ts.id
    tag = ts.tag
    taskData = ts.taskData.toJson.toString
    solution = ts.solution
    this
  }

  def toTask: Task = {
    Task(id, tag, (Json.parse(taskData)).validate[TaskData].fold({errors => null}, {taskData => taskData}), solution)
  }
}