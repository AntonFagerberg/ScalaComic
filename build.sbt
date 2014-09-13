name := """ComicBaker"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala,SbtWeb)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm
)

pipelineStages := Seq(rjs, digest, gzip)