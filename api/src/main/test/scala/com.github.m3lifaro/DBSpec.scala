package com.github.m3lifaro

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.m3lifaro.common.{NearMark, UserMark}
import com.github.m3lifaro.storage.{GeoCellNotFoundException, IgniteDB}
import com.github.m3lifaro.util.GeoServiceSupport
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}

class DBSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  val locationFileName = "users_marks_test.tsv"
  val geoFileName = "geo_cells_test.tsv"
  GeoServiceSupport.generateTest()

  implicit val system: ActorSystem = ActorSystem("geo-service-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val db = new IgniteDB(locationFileName, geoFileName)

  "ignite insert mark" should "insert mark and find it in cache" in {
    val mark = UserMark(10, 10)
    val f = db.createMark(mark)
    f flatMap { sum => db.findMark(sum.id) } map { elem ⇒ assert(elem == Option(mark)) }
  }

  "ignite remove mark" should "remove mark" in {
    val mark = UserMark(10, 10, 100)
    val f = db.deleteMark(mark)
    f flatMap { sum => db.findMark(100) } map { elem ⇒ assert(elem.isEmpty) }
  }

  "ignite find mark" should "insert mark and find it in cache" in {
    val f = db.findMark(100)
    f map { elem ⇒ assert(elem.isEmpty) }
  }

  "ignite update mark" should "update mark and find it in cache" in {
    val m = UserMark(-999d, 999d)
    val f = db.updateMark(UserMark(-999d, 999d))
    f flatMap { m ⇒ db.findMark(m.id) } map { elem ⇒ assert(elem.contains(m)) }
  }

  "ignite near mark" should "near mark not found" in {
    val mark = UserMark(10, 10)
    val f = db.nearMark(mark)
    f.recover {
      case GeoCellNotFoundException(e) ⇒ NearMark(isNear = false, "geo cell not found")
    } map { elem ⇒
      assert(elem == NearMark(isNear = false, "geo cell not found"))
    }
  }

  "ignite near mark" should "insert mark and near mark check" in {
    val mark = UserMark(100, 100)
    val f1 = db.createMark(mark)
    f1 flatMap { m ⇒ db.nearMark(m) } map { elem ⇒ assert(elem.isNear) }
  }


  "ignite find users near mark 100 100" should "find users check" in {
    val mark = UserMark(100, 100)
    val f1 = db.getUsersNearCell(100, 100)
    f1 map { elem ⇒ assert(elem.size == 1) }
  }

  "ignite find users near mark 45 20" should "find users check. should find all users" in {
    val f1 = db.getUsersNearCell(45, 20)
    f1 map { elem ⇒ assert(elem.size == 101) }
  }

}
