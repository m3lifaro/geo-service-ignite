package com.github.m3lifaro.common

import org.apache.ignite.cache.query.annotations.QuerySqlField

case class UserMark(@QuerySqlField(index=true)lat: Double, @QuerySqlField(index=true)lon: Double, id: Long = 0) {
  require(id >= 0)
  def toJava: UserMarkJ = {
    new UserMarkJ(lat,lon,id)
  }
}
object UserMarkFrom {
  def fromJava(jMark: UserMarkJ): UserMark = {
    UserMark(jMark.lat, jMark.lon, jMark.id)
  }
}
case class GeoCell(tile_x: Int, tile_y: Int, distance_error: Double) {
  require(distance_error > 0)
}

case class Border(lLat: Double, hLat: Double, lLon: Double, hLon: Double)

case class NearMark(isNear: Boolean, message: String)

case class Cell(tile_x: Int, tile_y: Int) {
  require(tile_x <= 180 && tile_x >= -180 && tile_y <= 90 && tile_y >= -90)
}