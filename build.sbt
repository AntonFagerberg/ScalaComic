name := """ComicBaker"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala,SbtWeb)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "mysql" % "mysql-connector-java" % "5.1.32",
  "org.apache.commons" % "commons-compress" % "1.8.1"
)

pipelineStages := Seq(rjs, digest, gzip)