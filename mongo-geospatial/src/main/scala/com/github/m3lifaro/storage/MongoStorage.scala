package com.github.m3lifaro.storage
import com.mongodb.{BasicDBList, BasicDBObject}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.model.Indexes._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
class MongoStorage(db: String) {

  private val codecRegistry = fromRegistries(fromProviders(classOf[UserMark], classOf[GeoCell], classOf[GeoPair]), DEFAULT_CODEC_REGISTRY )

  private val mongoClient: MongoClient = MongoClient()
  private val database: MongoDatabase = mongoClient.getDatabase(db).withCodecRegistry(codecRegistry)

  private val marksCollection: MongoCollection[UserMark] = database.getCollection("users_marks")
  private val cellsCollection: MongoCollection[Document] = database.getCollection("geo_cells")

  marksCollection.createIndex(geo2d("point")).toFutureOption().onComplete {
    case Success(e) ⇒ println(e)
    case Failure(e) ⇒ println(e)
  }

  def insertMark(mark: UserMark) = {
    marksCollection.insertOne(mark).toFuture()
  }

  def findMarksNear(lat: Double, lon: Double, radius: Double) = {
    val query = new BasicDBObject()
//    val loc = new BasicDBObject()
    val near = new BasicDBList()
    near.put( "0", lat )
    near.put( "1", lon )
//    loc.put("$near", near)
//    query.put("point",loc)

//    {"$within" : {"$center" : [[5.14,52.06], 0.0005]}}

    val center = new BasicDBList()
    center.put(0, near)
    center.put(1, radius)
    val centerQ = new BasicDBObject()
    centerQ.put("$center", center)
    val within = new BasicDBObject()
    within.put("$within", centerQ)
    query.put("point",within)
    marksCollection.find(query).toFuture()
  }
}
