package com.github.m3lifaro.common

trait GeoSupport {
  def orthodromic(location: UserMark, destination: UserMark): Double = {
    val earth_r = 6372795

    val u_lat = math.toRadians(location.lat)
    val d_lat = math.toRadians(destination.lat)
    val u_lon = math.toRadians(location.lon)
    val d_lon = math.toRadians(destination.lon)

    val uCos = math.cos(u_lat)
    val dCos = math.cos(d_lat)
    val uSin = math.sin(u_lat)
    val dSin = math.sin(d_lat)
    val delta = d_lon - u_lon
    val cos_delta = Math.cos(delta)
    val sin_delta = Math.sin(delta)
    val y = math.sqrt(math.pow(dCos * sin_delta, 2) + math.pow(uCos * dSin - uSin * dCos * cos_delta, 2))
    val x = uSin * dSin + uCos * dCos * cos_delta
    val ad = Math.atan2(y, x)
    ad * earth_r
  }

  val mether = 0.000009009d
}
