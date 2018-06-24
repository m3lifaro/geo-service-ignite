package com.github.m3lifaro

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.github.m3lifaro.rest.route.GeoServiceRoute
import com.github.m3lifaro.storage.IgniteDB
import com.github.m3lifaro.util.GeoServiceSupport
import org.scalatest._

class RouteSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with ScalatestRouteTest {
  val locationFileName = "users_marks_test.tsv"
  val geoFileName = "geo_cells_test.tsv"
  GeoServiceSupport.generateTest()


  val db = new IgniteDB(locationFileName, geoFileName)

  val routes: Route = new GeoServiceRoute(db).route

  "GeoRoute" should {
    "Posting to api/mark/create_mark should add the event" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "lat":100.0,
           |    "lon":100.0,
           |    "id":0
           |}
        """.stripMargin)

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/api/mark/create_mark",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> routes ~> check {
        status.isSuccess() shouldEqual true
      }
    }
  }
}