name := """scalaTest"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.scala-lang" % "scala-compiler" % "2.11.6",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "com.db4o" % "com.db4o" % "7.7.67"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "db4o-repo" at "http://maven.restlet.org/"
//resolvers += "twitter-repo" at "http://maven.twttr.com/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator