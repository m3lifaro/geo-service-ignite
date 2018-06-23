package com.github.m3lifaro.storage.dao
import com.github.m3lifaro.common.GeoCell
import com.github.m3lifaro.storage.IgniteDB
import scala.collection.JavaConverters._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GeoCellDaoIgnite {

  private val geoCellCache = IgniteDB.geoCellIgniteCache
//  private val geoCellIds = IgniteDB.geoCellSequence

  def findGeoCell(lat: Int, lon: Int): Future[Option[GeoCell]] = {
//  def findGeoCell(lat: Int, lon: Int, geoCellIdsCache: Map[(Int,Int), Int]): Future[Option[GeoCell]] = {
    Future {
//      geoCellIdsCache.get(lon, lat).map(id ⇒ geoCellCache.get(id))
      Option(geoCellCache.get((lon,lat)))
    }
  }

//  def create(id: Long, cell: GeoCell): Unit = {
  def create(cell: GeoCell): Unit = {
      geoCellCache.put((cell.tile_x,cell.tile_y), cell)
  }
}
