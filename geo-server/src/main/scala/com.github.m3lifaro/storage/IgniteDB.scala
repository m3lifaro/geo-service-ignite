package com.github.m3lifaro.storage

import java.nio.file.Paths

import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import com.github.m3lifaro.common._
import com.github.m3lifaro.storage.dao.{GeoCellDaoIgnite, UserMarkDaoIgnite}
import com.typesafe.scalalogging.StrictLogging
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.{IgniteAtomicSequence, IgniteCache}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class IgniteDB(locationFileName: String, geoFileName: String)(implicit m: Materializer) extends GeoSupport with StrictLogging {
  val cellDao = new GeoCellDaoIgnite
  val marksDao = new UserMarkDaoIgnite

  fillCache(locationFileName, geoFileName)

  def nearMark(location: UserMark): Future[NearMark] = {
    marksDao.findUser(location.id).flatMap {
      case Some(mark) ⇒
        cellDao.findGeoCell(mark.lat.toInt, mark.lon.toInt).flatMap {
          case Some(cell) ⇒
            Future {
              val o = orthodromic(location, mark)
              if (o <= cell.distance_error)
                NearMark(isNear = true, s"location [$location] near mark [$mark] \n(distance: $o , location error: ${cell.distance_error})")
              else
                NearMark(isNear = false, s"location: $location not even close mark: $mark : you need to go ${o - cell.distance_error} meters more")
            }
          case None ⇒ Future.failed(GeoCellNotFoundException(s"geo cell (tile_x: ${mark.lon.toInt} tile_y: ${mark.lat.toInt}) not found"))
        }

      case None ⇒ Future.failed(UserMarkNotFoundException(s"user with id: ${location.id} not found"))
    }
  }

  def getUsersNearCell(lat: Int, lon: Int): Future[Seq[UserMark]] = {
    cellDao.findGeoCell(lat, lon) flatMap {
      case Some(cell) ⇒
        logger.info(s"founded cell: $cell")
        val d = cell.distance_error * mether
        val b = Border(lLat = cell.tile_y - d, hLat = cell.tile_y + d, lLon = cell.tile_x - d, hLon = cell.tile_x + d)
        marksDao.findUsersNearMark(b)
      case None ⇒ Future.failed(GeoCellNotFoundException(s"geo cell (tile_x: $lon tile_y: $lat) not found"))
    }
  }

  def createMark(mark: UserMark): Future[UserMark] = {
    marksDao.createUser(mark)
  }

  def updateMark(mark: UserMark): Future[UserMark] = {
    marksDao.updateUser(mark)
  }

  def deleteMark(mark: UserMark): Future[Unit] = {
    marksDao.deleteUser(mark)
  }

  def findMark(id: Long): Future[Option[UserMark]] = {
    marksDao.findUser(id)
  }

  private def getStream[T](file: String)(transformer : String ⇒ T) = FileIO.fromPath(Paths.get(file))
    .via(Framing.delimiter(ByteString("\n"), 512, allowTruncation = true)
        .filter(_.nonEmpty)
      .map(_.utf8String))
    .map(transformer)

  private def getLocationStream(file: String): Source[UserMark, Future[IOResult]] = getStream(file){ elem ⇒
    val splitted = elem.split("\t")
    UserMark(splitted(2).toDouble, splitted(1).toDouble, splitted(0).toInt)
  }

  private def getGeoStream(file: String): Source[GeoCell, Future[IOResult]] = getStream(file){ elem ⇒
    val splitted = elem.split("\t")
    GeoCell(splitted(0).toInt, splitted(1).toInt, splitted(2).toDouble)
  }

  private def fillCache(marksPath: String, geoCellPath: String): Unit = {

    logger.info("Initiating cache")

    val cellFuture = getGeoStream(geoCellPath).runWith(Sink.foreach { cell ⇒
      cellDao.create(cell)
    })

    val markFuture = getLocationStream(marksPath).grouped(100000).runWith(Sink.foreach{ marks ⇒
      marksDao.createAll(marks)
    })

    val f = Future.sequence(Seq(cellFuture, markFuture))
    Await.result(f, Duration.Inf)

    logger.info("Init complete")
  }
}

object IgniteDB {

  import org.apache.ignite.Ignition
  import org.apache.ignite.configuration.IgniteConfiguration

  Ignition.getOrStart(new IgniteConfiguration)

  def apply(locationFileName: String, geoFileName: String)(implicit m: Materializer): IgniteDB =
    new IgniteDB(locationFileName, geoFileName)
  private[storage] def userMarkSequence: IgniteAtomicSequence =
    Ignition.ignite.atomicSequence("userMarkSequence", 0, true)

  def userMarkIgniteCache: IgniteCache[Long, UserMarkJ] =
    Ignition.ignite.getOrCreateCache(userMarkCacheConfiguration)

  def geoCellIgniteCache: IgniteCache[(Int,Int), GeoCell] =
    Ignition.ignite.getOrCreateCache(geoCellCacheConfiguration)

  private val geoCellCacheConfiguration: CacheConfiguration[(Int,Int), GeoCell] = {
    val cc = new CacheConfiguration[(Int, Int), GeoCell]
    cc.setName("geoCell")
    cc.setIndexedTypes(classOf[(Int, Int)], classOf[GeoCell])
    cc
  }

  private val userMarkCacheConfiguration: CacheConfiguration[Long, UserMarkJ] = {
    val cc = new CacheConfiguration[Long, UserMarkJ]
    cc.setName("userMark")
    cc.setIndexedTypes(classOf[Long], classOf[UserMarkJ])
    cc
  }
}

case class GeoCellNotFoundException(msg: String) extends Exception(msg)
case class UserMarkNotFoundException(msg: String) extends Exception(msg)