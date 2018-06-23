package com.github.m3lifaro.util

import java.nio.file.{Files, Paths}

object GeoServiceSupport {

  def ifExists(marksPath: String, geoCellPath: String): Boolean = {
    Files.exists(Paths.get(marksPath)) && Files.exists(Paths.get(geoCellPath))
  }

  def generate(): Unit = {
    val rnd = new scala.util.Random
    val randomValue = 20 + (100 - 20) * rnd.nextDouble()

    val r = for {
      halfLat ← -45 to 45
      halfLon ← -90 to 90
      error = 20 + (100 - 20) * rnd.nextDouble()
    } yield (halfLon, halfLat, error)
    r.size

    import java.io._
    val pw = new PrintWriter(new File("geo_cells.tsv"))
    r.foreach { case (tile_x: Int, tile_y: Int, err: Double) ⇒
      pw.write(s"$tile_x\t$tile_y\t$err\n")
    }
    pw.close()

    val stream = (0 until 10000000).toStream.map(elem => (elem, -45 + 90 * rnd.nextDouble(), -90 + 180 * rnd.nextDouble()))

    val pw2 = new PrintWriter(new File("users_marks.tsv"))
    stream.foreach { case (index: Int, lat: Double, lon: Double) ⇒
      pw2.write(s"$index\t$lon\t$lat\n")
    }
    pw2.close()
  }

  def generateTest(): Unit = {
    val rnd = new scala.util.Random

    val r = for {
      halfLat ← 0 to 10
      halfLon ← 0 to 0
      error = 10000d
    } yield (halfLon, halfLat, error)

    import java.io._
    val pw = new PrintWriter(new File("geo_cells_test.tsv"))
    r.foreach { case (tile_x: Int, tile_y: Int, err: Double) ⇒
      pw.write(s"$tile_x\t$tile_y\t$err\n")
    }
    pw.write(s"100\t100\t1000\n")
    pw.write(s"20\t45\t9999999999999999\n")
    pw.close()

    //    val stream = (1 to 1000000).toStream.map(elem => (elem, -45 + (90) * rnd.nextDouble(), -90 + (180) * rnd.nextDouble()))
    val stream = (0 until 100).toStream.map(elem => (elem, 40 * rnd.nextDouble(), 90 * rnd.nextDouble()))

    val pw2 = new PrintWriter(new File("users_marks_test.tsv"))
    stream.foreach { case (index: Int, lat: Double, lon: Double) ⇒
      pw2.write(s"$index\t$lon\t$lat\n")
    }
    pw2.close()
  }
}
