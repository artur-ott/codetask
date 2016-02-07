package models.hibernate

import models.tasks._
import models._
import javax.persistence._
import org.hibernate.annotations.GenericGenerator
import scala.collection.JavaConverters._
import models.tasks.Tasks._
import play.api.libs.json._

// necessery for composit key
class UserName extends Serializable {
  var id: Long = _
  var username: String = _
}

@Entity
@Table(name = "UserHibernate")
//@NamedQueries(Array(
//  new NamedQuery(name="User.findOneById", query="from UserHibernate where id=:id")
//))
@IdClass(classOf[UserName])
class UserHibernate extends Serializable {
  @Id 
  var id: Long = _
  @Id 
  var username: String = _
  var authority: String = _
  var password: String = _

  //@ElementCollection(targetClass = classOf[ChapterSolutionHibernate])
  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "userH", orphanRemoval = true)
  var chapterSolutions: java.util.List[ChapterSolutionHibernate] = _

  @ElementCollection(targetClass = classOf[Long])
  var subscriptions: java.util.List[Long] = _

  def fill(user: User): UserHibernate = {
    id = user.id 
    username = user.username
    authority = user.authority 
    password = user.password 

    val css = user.chapterSolutions.map{x => (new ChapterSolutionHibernate).fill(x, this)}.asJava
    chapterSolutions match {
      case null => chapterSolutions = css
      case _ => chapterSolutions.clear(); chapterSolutions.addAll(css)
    }

    subscriptions = user.subscriptions.toList.asJava
    this
  }

  def toUser: User = {
    User(id, username, authority, password, chapterSolutions.asScala.map{x => x.toChapterSolution}.toList, subscriptions.asScala.toSet)
  }
}

@Entity
@Table(name = "ChapterSolutionHibernate")
class ChapterSolutionHibernate extends Serializable {
  @Id
  @GenericGenerator(name="generator", strategy="increment")
  @GeneratedValue(generator="generator")
  var id: Long =_
  var courseId: Long = _
  var chapterId: Long = _

  @ManyToOne
  var userH: UserHibernate = _

  //@ElementCollection(targetClass = classOf[TaskSolutionHibernate])
  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "chapterSolutionH", orphanRemoval = true)
  var taskSolutions: java.util.List[TaskSolutionHibernate] = _

  def fill(cs: ChapterSolution, uh: UserHibernate): ChapterSolutionHibernate = {
    userH = uh
    courseId = cs.courseId
    chapterId = cs.chapterId

    val sts = cs.taskSolutions.map{x => (new TaskSolutionHibernate).fill(x, this)}.toList.asJava
    taskSolutions match {
      case null => taskSolutions = sts
      case _ => taskSolutions.clear(); taskSolutions.addAll(sts)
    }
    this
  }

  def toChapterSolution: ChapterSolution = {
    ChapterSolution(courseId, chapterId, taskSolutions.asScala.map{x => x.toTaskSolution}.toList)
  }
}

@Entity
@Table(name = "TaskSolutionHibernate")
class TaskSolutionHibernate extends Serializable {
  @Id
  @GenericGenerator(name="generator", strategy="increment")
  @GeneratedValue(generator="generator")
  var id: Long =_
  var taskId: String = _
  var taskState: String = _
  var checked: Option[Boolean] = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  var chapterSolutionH: ChapterSolutionHibernate = _

  def fill(ts: TaskSolution, csh: ChapterSolutionHibernate): TaskSolutionHibernate = {
    chapterSolutionH = csh
    taskId = ts.taskId
    taskState = ts.taskState.toJson.toString
    checked = ts.checked
    this
  }

  def toTaskSolution: TaskSolution = {
    TaskSolution(taskId, (Json.parse(taskState)).validate[TaskState].fold({errors => null}, {taskstate => taskstate}), checked)
  }
}