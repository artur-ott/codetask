package models

import play.api.libs.json._

class Course(var name: String, var jsObj: JsObject)

trait CourseRepository {
	def findOneByName(name: String): Option[Course]
	def findAll(): List[Course]
	def create(course: Course): Option[Course]
	def update(course: Course): Option[Course]
	def delete(course: Course): Option[Course]
}

class CourseService(env: {val courseRepository: CourseRepository}) {
	def findOneByName(name: String) =
		env.courseRepository.findOneByName(name)
	def findAll() =
		env.courseRepository.findAll()
	def create(course: Course) =
		env.courseRepository.create(course)
	def update(course: Course) =
		env.courseRepository.update(course)
	def delete(course: Course) =
		env.courseRepository.delete(course)
}