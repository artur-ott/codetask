package models

class CourseService(env: {val courseRepository: CourseRepository}) {
	def findOneByTitle(name: String) =
		env.courseRepository.findOneByTitle(name)
	def findOneById(id: Long) = 
		env.courseRepository.findOneById(id)
	def findAll() =
		env.courseRepository.findAll()
	def create(course: Course) =
		env.courseRepository.create(course)
	def update(course: Course) =
		env.courseRepository.update(course)
	def delete(course: Course) =
		env.courseRepository.delete(course)
	def getId(): Long = {
		val courses = findAll()
		var id = 100000
		do { id += 1 } while (courses.find(c => c.id == id) != None)
		id
	}
}