import play.PlayScala

name := """play-qb"""

version := "1.0-SNAPSHOT"

resolvers += "QB repository" at "http://dl.bintray.com/qbproject/maven"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.qbproject"     %% "qbschema"      % "0.4.1.2",
  "org.qbproject"     %% "qbplay"        % "0.4.1.2",
  "com.github.athieriot" %% "specs2-embedmongo" % "0.7.0"
)
