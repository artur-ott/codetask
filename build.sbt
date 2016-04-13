
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  javaJpa,
  cache,
  ws,
  specs2 % Test,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.9.Final",
  "org.scala-lang" % "scala-compiler" % "2.11.6",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "com.db4o" % "com.db4o" % "7.7.67",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.spire-math" % "spire_2.11" % "0.11.0"
)

scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "db4o-repo" at "http://maven.restlet.org/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator