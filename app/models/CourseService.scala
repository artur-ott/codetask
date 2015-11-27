package models

class CourseService(env: {val courseRepository: CourseRepository}) {
	def getOneByName(name: String) =
		env.courseRepository.getOneByName(name)
	def create(course: Course) =
		env.courseRepository.create(course)
	def update(course: Course) =
		env.courseRepository.update(course)
	def delete(course: Course) =
		env.courseRepository.delete(course)
}