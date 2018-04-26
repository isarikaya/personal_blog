name := """blog"""
organization := "com.paipiper"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8"
libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.6"