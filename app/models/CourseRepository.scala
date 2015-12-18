package models

trait CourseRepository {
	def findOneByTitle(name: String): Option[Course]
	def findOneById(id: Long): Option[Course]
	def findAll(): List[Course]
	def create(course: Course): Option[Course]
	def update(course: Course): Option[Course]
	def delete(course: Course): Option[Course]
}