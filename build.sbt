name := """dbeditor-server"""
organization := "ru.ifmo.se"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, PlayEbean)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1211"