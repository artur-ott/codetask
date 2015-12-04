package models

class Course(var name: String, var chapters: List[Chapter])

class Chapter(var tasks: String)

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