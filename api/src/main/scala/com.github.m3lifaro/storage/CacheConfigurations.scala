package com.github.m3lifaro.storage

import com.github.m3lifaro.common.{GeoCell, UserMark, UserMarkJ}
import org.apache.ignite.configuration.CacheConfiguration

object CacheConfigurations {

  val geoCellCacheConfiguration: CacheConfiguration[(Int,Int), GeoCell] = {
    val cc = new CacheConfiguration[(Int, Int), GeoCell]
    cc.setName("geoCell")
    cc.setIndexedTypes(classOf[(Int, Int)], classOf[GeoCell])
    cc
  }

  val userMarkCacheConfiguration: CacheConfiguration[Long, UserMarkJ] = {
    val cc = new CacheConfiguration[Long, UserMarkJ]
    cc.setName("userMark")
    cc.setIndexedTypes(classOf[Long], classOf[UserMarkJ])
    cc
  }

}
