package com.github.m3lifaro

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import com.github.m3lifaro.rest.route.GeoServiceRoute
import com.github.m3lifaro.storage.IgniteDB
import com.github.m3lifaro.util.GeoServiceSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object Node extends App with StrictLogging {

  //  GeoServiceSupport.generate()

  val params = args.toList match {
    case "--mark" :: markPath :: "--cell" :: cellPath :: tail => Some(markPath, cellPath)
    case _ ⇒ logger.info("Usage: mmlaln [--mark markfilepath] [--cell cellfilepath] "); None
  }
  params match {
    case Some((locationFileName, geoFileName)) ⇒

      if (GeoServiceSupport.ifExists(locationFileName, geoFileName)) {

        implicit val system: ActorSystem = ActorSystem("geo-service-system")
        implicit val materializer: ActorMaterializer = ActorMaterializer()
        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        val igniteDB = new IgniteDB(locationFileName, geoFileName)

        val routes = new GeoServiceRoute(igniteDB).route

        logger.info("Application started")

        implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
          case ex ⇒ extractUri { uri ⇒
            logger.error(s"Got error in route $uri", ex)
            complete(HttpResponse(StatusCodes.InternalServerError, entity = s"Got error in route $uri"))
          }
        }

        val f = Http().bindAndHandle(routes, "localhost", 9090)
        Await.ready(system.whenTerminated, Duration.Inf)
        f.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

      } else {
        logger.error("db_files not found")
      }
    case None ⇒
  }


}