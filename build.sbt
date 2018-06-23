import Dependencies._
import sbt.Keys.version

name := "geo-service"

version := "0.2"

lazy val commonSettings = Seq(
  organization := "com.github.m3lifaro",
  scalaVersion := "2.12.4"
)

lazy val api = (project in file("api")).settings(
  commonSettings,
  name := "api",
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