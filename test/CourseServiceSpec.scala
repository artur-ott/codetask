import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import models.CodeTask
import models.Execution
import models._

class CourseServiceSpec extends Specification {
	val courseService = new CourseService(Config);
	val id = courseService.getId()
	val json1 = "{\"t\": 1 }"
	val course1 = new Course(id, "scala", json1)

	"CourseService#findOneByName" should {
		"fail with unknown course name" in {
			val course = courseService.findOneByName("no name")
			course should be(None)
		}
	}
	"CourseService#create" should {
		"succeed when course doesn't exist" in {
			courseService.create(course1) should be equalTo(Some(course1))
		}
		"fail when course does exist" in {
			courseService.create(course1) should be(None)
		}
	}
	"CourseService#findOneByName" should {
		"find course after creation" in {
			val course = courseService.findOneByName(course1.name)
			course.get.id must equalTo(course1.id)
		}
	}
	"CourseService#findOneById" should {
		"find course after creation" in {
			val course = courseService.findOneById(course1.id)
			course.get.name must equalTo(course1.name)
		}
	}
	"CourseService#findAll" should {
		"give all courses" in {
			courseService.findAll().size shouldEqual(1)
		}
	}
	"CourseService#update" should {
		"work with existing course" in {
			val json = "{\"t\": 2 }"
			course1.json = json
			courseService.update(course1)
			val course = courseService.findOneByName("scala").get
			course.json must be equalTo(json)
		}
	}
	"CourseService#delete" should {
		"succeed with existing course" in {
			var course = courseService.findOneByName("scala")
			courseService.delete(course.get)
			courseService.findOneByName("scala") should be(None)
		}
	}
}