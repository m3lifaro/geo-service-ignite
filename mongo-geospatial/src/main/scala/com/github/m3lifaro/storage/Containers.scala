package com.github.m3lifaro.storage

import org.mongodb.scala.bson.ObjectId

case class GeoPair(lat: Double, lon: Double)
case class UserMark(_id: ObjectId, point: GeoPair, id: Long)
object UserMark {
  def apply(lat: Double, lon: Double, id: Long = 0): UserMark = new UserMark(new ObjectId(), GeoPair(lat, lon), id)
}
case class GeoCell(_id: ObjectId, tile_x: Int, tile_y: Int, distance_error: Double)
object GeoCell {
  def apply(tile_x: Int, tile_y: Int, distance_error: Double): GeoCell = new GeoCell(new ObjectId(), tile_x, tile_y, distance_error)
}


