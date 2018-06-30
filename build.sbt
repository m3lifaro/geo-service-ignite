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
  assemblyJarName in assembly := "geo-service.jar",
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

lazy val geospatial = (project in file("mongo-geospatial")).settings(
  commonSettings,
  name := "mongo-geospatial-service",
  assemblyJarName in assembly := "mongo-geospatial-service.jar",
  scalacOptions in Test ++= Seq("-Yrangepos"),
  libraryDependencies ++= Seq(
    finchCore,
    finchCirce,
    akkaStream,
    mongodb)
    ++ Dependencies.Logging
)