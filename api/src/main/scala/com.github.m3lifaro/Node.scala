package com.github.m3lifaro

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.m3lifaro.rest.route.UserRoute
import com.github.m3lifaro.storage.IgniteDB
import com.github.m3lifaro.util.GeoServiceSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor

object Node extends App with StrictLogging {

//  GeoServiceSupport.generateTest()

  //todo будут получаться из аргументов ком. строки
  val locationFileName = "users_marks.tsv"
  val geoFileName = "geo_cells.tsv"

  if (GeoServiceSupport.ifExists(locationFileName, geoFileName)) {

    implicit val system: ActorSystem = ActorSystem("geo-service-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val igniteDB = new IgniteDB(locationFileName, geoFileName)

    val routes = new UserRoute(igniteDB).route

    logger.info("Application started")

    Http().bindAndHandle(routes, "localhost", 9090)
  } else {
    logger.error("db_files not found")
  }

}