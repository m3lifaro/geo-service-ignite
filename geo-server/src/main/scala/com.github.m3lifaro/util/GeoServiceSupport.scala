package com.github.m3lifaro.util

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}
import java.util.function.IntConsumer
import java.util.stream.IntStream

import com.typesafe.scalalogging.StrictLogging

import scala.annotation.tailrec

object GeoServiceSupport extends StrictLogging {

  def ifExists(marksPath: String, geoCellPath: String): Boolean = {
    Files.exists(Paths.get(marksPath)) && Files.exists(Paths.get(geoCellPath))
  }

  def generate(markFile: String, cellFile: String): Unit = {
    val rnd = new scala.util.Random

    val r = for {
      halfLat ← -45 to 45
      halfLon ← -90 to 90
      error = 20 + (100 - 20) * rnd.nextDouble()
    } yield (halfLon, halfLat, error)

    logger.info(s"creating '$cellFile'")
    import java.io._
    val pw = new PrintWriter(new File(cellFile))
    r.foreach { case (tile_x: Int, tile_y: Int, err: Double) ⇒
      pw.write(s"$tile_x\t$tile_y\t$err\n")
    }
    pw.close()
    logger.info(s"done creating '$cellFile'")

    val stream = (0 until 10000000).toStream.map(elem => (elem, -45 + 90 * rnd.nextDouble(), -90 + 180 * rnd.nextDouble()))
      .map{ case (index: Int, lat: Double, lon: Double) ⇒
        s"$index\t$lon\t$lat\n"
      }

    @tailrec
    def process(stream: Stream[String], writer: Writer, iteration: Int): Unit = {
      if (stream.nonEmpty) {
        val  builder = new StringBuffer()
        stream.take(100000).foreach(builder.append)
        writer.write(builder.toString)
        process(stream.drop(100000), writer, iteration + 1)

      } else {
        writer.close()
      }
    }

    logger.info(s"creating '$markFile'")

    val pw2 = new PrintWriter(new File(markFile))

    process(stream, pw2, 1)

    logger.info(s"done creating '$markFile'")
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

    val stream = (0 until 100).toStream.map(elem => (elem, 40 * rnd.nextDouble(), 90 * rnd.nextDouble()))

    val pw2 = new PrintWriter(new File("users_marks_test.tsv"))
    stream.foreach { case (index: Int, lat: Double, lon: Double) ⇒
      pw2.write(s"$index\t$lon\t$lat\n")
    }
    pw2.close()
  }
}
