package com.github.m3lifaro.storage.dao
import com.github.m3lifaro.common.GeoCell
import com.github.m3lifaro.storage.IgniteDB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GeoCellDaoIgnite {

  private val geoCellCache = IgniteDB.geoCellIgniteCache

  def findGeoCell(lat: Int, lon: Int): Future[Option[GeoCell]] = {
    Future {
      Option(geoCellCache.get((lon,lat)))
    }
  }

  def create(cell: GeoCell): Unit = {
      geoCellCache.put((cell.tile_x,cell.tile_y), cell)
  }
}
