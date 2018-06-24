import Dependencies._
import sbt.Keys.version

name := "geo-service"

version := "0.2"

lazy val commonSettings = Seq(
  organization := "com.github.m3lifaro",
  scalaVersion := "2.12.4"
)

lazy val server = (project in file("geo-server")).settings(
  commonSettings,
  name := "geo-service",
  assemblyJarName in assembly := s"geo-service.jar",
  scalacOptions in Test ++= Seq("-Yrangepos"),
  libraryDependencies ++= Seq(
    akkaHttp,
    akkaHttpSpray,
    akkaStream,
    akkaHttpTestKit,
    akkaTestKit)
    ++ Dependencies.TestKit
    ++ Dependencies.Logging
    ++ Dependencies.Ignite
)