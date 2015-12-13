package models

object Services {
    val userService = new UserService(Config)
    val courseService = new CourseService(Config)
}