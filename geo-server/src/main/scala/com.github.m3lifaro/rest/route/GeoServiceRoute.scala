package com.github.m3lifaro.rest.route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import com.github.m3lifaro.common.{Cell, UserMark}
import com.github.m3lifaro.rest.RestJsonSupport
import com.github.m3lifaro.storage.IgniteDB
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
class GeoServiceRoute(DB: IgniteDB)(implicit executionContext: ExecutionContext, m: Materializer) extends Directives with StrictLogging with RestJsonSupport {

  val route: Route = locate ~ nearCell ~ createMark ~ updateMark ~ deleteMark()

  def locate: Route = pathPrefix("api/mark/locate") {
    post {
      entity(as[UserMark]) { location =>
        onComplete(DB.nearMark(location)) {
          case Success(resp) ⇒
            complete(resp)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }

  def nearCell: Route = path("api/mark/near") {
    post {
      entity(as[Cell]) { cell =>
        onComplete(DB.getUsersNearCell(cell.tile_y, cell.tile_x)) {
          case Success(users) ⇒
            complete(users)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }

  def createMark: Route = pathPrefix("api/mark/create_mark") {
    post {
      entity(as[UserMark]) { location =>
        onComplete(DB.createMark(location)) {
          case Success(resp) ⇒
            complete(resp)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }

  def updateMark: Route = pathPrefix("api/mark/update_mark") {
    put {
      entity(as[UserMark]) { location =>
        onComplete(DB.updateMark(location)) {
          case Success(resp) ⇒
            complete(resp)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }

  def deleteMark(): Route = pathPrefix("api/mark/delete_mark") {
    delete {
      entity(as[UserMark]) { location =>
        onComplete(DB.deleteMark(location)) {
          case Success(_) ⇒
            complete(StatusCodes.OK)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }
}
