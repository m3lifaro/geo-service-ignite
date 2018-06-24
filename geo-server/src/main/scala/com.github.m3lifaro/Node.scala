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
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import Directives._
object Node extends App with StrictLogging {

  val params = args.toList match {
    case "--mark" :: markPath :: "--cell" :: cellPath :: tail => Some(markPath, cellPath)
    case "--generate" :: markfilename :: cellfilename :: tail ⇒ logger.info("generating files"); GeoServiceSupport.generate(markfilename, cellfilename); None
    case _ ⇒ logger.info("Usage: [--mark markfilepath --cell cellfilepath] | [--generate markfilename cellfilename]"); None
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
implicit def myRejectionHandler =
  RejectionHandler.newBuilder()
    .handle { case MissingCookieRejection(cookieName) =>
      complete(HttpResponse(BadRequest, entity = "No cookies, no service!!!"))
    }
    .handle { case AuthorizationFailedRejection =>
      complete((Forbidden, "You're out of your depth!"))
    }
    .handle { case ValidationRejection(msg, _) =>
      complete((InternalServerError, "That wasn't valid! " + msg))
    }
    .handleAll[MethodRejection] { methodRejections =>
    val names = methodRejections.map(_.supported.name)
    complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
  }
    .handleNotFound { complete((NotFound, "Not here!")) }
    .result()

        val f = Http().bindAndHandle(routes, "localhost", 9090)
        Await.ready(system.whenTerminated, Duration.Inf)
        f.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

      } else {
        logger.error("db_files not found")
      }
    case None ⇒
  }


}