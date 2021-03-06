package models

import scala.language.reflectiveCalls

class CourseService(env: {val courseRepository: CourseRepository}) {
  def findOneByTitle(name: String): Option[Course] =
    env.courseRepository.findOneByTitle(name)
  def findOneById(id: Long): Option[Course] = 
    env.courseRepository.findOneById(id)
  def findAll(): List[Course] =
    env.courseRepository.findAll()
  def create(course: Course): Option[Course] =
    env.courseRepository.create(course)
  def update(course: Course): Option[Course] =
    env.courseRepository.update(course)
  def delete(course: Course): Option[Course] =
    env.courseRepository.delete(course)
}