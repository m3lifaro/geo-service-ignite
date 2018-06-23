import sbt._

object Dependencies {
  val akkaVersion = "2.5.11"
  val akkaHttpVersion = "10.1.1"
  val logbackVersion = "1.2.3"
  val scalaLoggingVersion = "3.9.0"
  val igniteVersion = "2.5.0"

  val akkaHttp = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaHttpSpray = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test

  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion

  val scalaTest = "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
  val specs2 = "org.specs2" %% "specs2-core" % "4.2.0" % "test"
  val igniteCore = "org.apache.ignite" % "ignite-core" % igniteVersion
  val igniteIndexing = "org.apache.ignite" % "ignite-indexing" % igniteVersion

  val TestKit = Seq(scalaTest, specs2)
  val Logging = Seq(logback, scalaLogging)
  val Ignite = Seq(igniteCore, igniteIndexing)
}
