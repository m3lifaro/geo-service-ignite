package com.github.m3lifaro.storage.dao
import com.github.m3lifaro.common.{Border, UserMark, UserMarkFrom, UserMarkJ}
import com.github.m3lifaro.storage.IgniteDB

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class UserMarkDaoIgnite {
  private val userMarkCache = IgniteDB.userMarkIgniteCache
  private val userMarkIds = IgniteDB.userMarkSequence

  def findUser(userId: Long): Future[Option[UserMark]] = {
    Future {
      Option(userMarkCache.get(userId)).map(UserMarkFrom.fromJava)
    }
  }

  def findUsersNearMark(border: Border): Future[Seq[UserMark]] = Future {
    import org.apache.ignite.cache.query.SqlQuery
    val qry: SqlQuery[Long, UserMarkJ] = new SqlQuery(classOf[UserMarkJ], s"lat >= ${border.lLat} and lat <= ${border.hLat} and lon >= ${border.lLon} and lon <= ${border.hLon}")

    userMarkCache.query(qry).getAll.asScala.toList.map(_.getValue).map(UserMarkFrom.fromJava)
  }


  def createUser(user: UserMark): Future[UserMark] = {
    Future {
      val id = userMarkIds.getAndIncrement()
      Try(userMarkCache.put(id, user.toJava)) match {
        case Success(_) ⇒ user.copy(id = id)
        case Failure(e) ⇒ println(e); throw e
      }

    }
  }

  def updateUser(user: UserMark): Future[UserMark] = {
    Future{
      userMarkCache.put(user.id, user.toJava)
      user
    }
  }

  def deleteUser(user: UserMark): Future[Unit] = {
    Future{
      userMarkCache.remove(user.id)
    }
  }

  def createAll(users: Seq[UserMark]): Unit = {
      val map = users.map(user ⇒ userMarkIds.getAndIncrement() → user.toJava).toMap.asJava
      userMarkCache.putAll(map)
  }
}
