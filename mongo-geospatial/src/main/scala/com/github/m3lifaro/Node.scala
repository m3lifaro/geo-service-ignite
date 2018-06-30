package com.github.m3lifaro

import com.github.m3lifaro.storage.{MongoStorage, UserMark}
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch._
import io.finch.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Node extends App {



  val storage = new MongoStorage("testdb")


  val m = UserMark(10.0,10.0)


 storage.findMarksNear(10,10,0.0005) onComplete {
    case Success(c) ⇒
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
      println(c)
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
    case Failure(e) ⇒
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
      println("failed: " + e)
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
  }

  val api: Endpoint[String] = get("hello") { Ok("Hello, World!") }

  val server = Http.server.serve(":9090", api.toServiceAs[Text.Plain])

  Await.ready(server)
}

