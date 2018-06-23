package com.github.m3lifaro.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.m3lifaro.common.{NearMark, UserMark}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait RestJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val simpleEventFormat: RootJsonFormat[UserMark] = jsonFormat3(UserMark)
  implicit val nearMarkFormat: RootJsonFormat[NearMark] = jsonFormat2(NearMark)

}
