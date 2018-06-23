package com.github.m3lifaro.rest.route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import com.github.m3lifaro.common.UserMark
import com.github.m3lifaro.rest.RestJsonSupport
import com.github.m3lifaro.storage.IgniteDB
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
class UserRoute(DB: IgniteDB)(implicit executionContext: ExecutionContext, m: Materializer) extends Directives with StrictLogging with RestJsonSupport {

  val route: Route = locate ~ createOrUpdateMark ~ deleteMark()

  def locate: Route = pathPrefix("api/user/locate") {
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

  def createOrUpdateMark: Route = pathPrefix("api/user/update_mark") {
    post {
      entity(as[UserMark]) { location =>
        onComplete(DB.createMark(location)) {
          case Success(resp) ⇒
            complete(StatusCodes.Created -> resp.toString)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    } ~
    put {
      entity(as[UserMark]) { location =>
        onComplete(DB.updateMark(location)) {
          case Success(resp) ⇒
            complete(StatusCodes.OK -> resp.toString)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }

  def deleteMark(): Route = pathPrefix("api/user/delete_mark") {
    delete {
      entity(as[UserMark]) { location =>
        onComplete(DB.deleteMark(location)) {
          case Success(resp) ⇒
            complete(StatusCodes.OK -> resp.toString)
          case Failure(err) ⇒
            logger.error("Some error occurred", err)
            complete(StatusCodes.BadRequest -> err.getMessage)
        }
      }
    }
  }
}
