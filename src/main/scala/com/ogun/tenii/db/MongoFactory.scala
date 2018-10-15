package com.ogun.tenii.db

import java.util.Date

import com.mongodb.casbah.Imports._
import com.ogun.tenii.domain.common.{Address, Passport, RoarType}
import com.typesafe.scalalogging.LazyLogging

import scala.util.Properties

object MongoFactory {
  private val DATABASE = Properties.envOrElse("MONGO_DB", "tenii-customer")
  val uri = MongoClientURI(s"mongodb://${Properties.envOrElse("MONGO_HOST", "localhost:27017")}/$DATABASE")
  val mongoClient = MongoClient(uri)
  val db = mongoClient(DATABASE)

  def getCollection(collection: String) = db(collection)
}

trait ObjectMongoConnection[A] extends LazyLogging {

  val collection: String

  def save(obj: A) = {
    val coll = retrieveCollection
    val toPersist = transform(obj)
    coll.save(toPersist)
  }

  protected def retrieveCollection = {
    MongoFactory.getCollection(collection)
  }

  protected def findByProperty(name: String, value: String, error: String) = {
    retrieveCollection.findOne(MongoDBObject(name -> value)) match {
      case Some(data) => Some(revert(data))
      case _ =>
        logger.error(error)
        None
    }
  }

  protected def findByObjectId(value: String, error: String) = {
    retrieveCollection.findOne(MongoDBObject("_id" -> new ObjectId(value))) match {
      case Some(data) => Some(revert(data))
      case _ =>
        logger.error(error)
        None
    }
  }

  protected def findByProperty(name: String, value: Int, error: String) = {
    retrieveCollection.findOne(MongoDBObject(name -> value)) match {
      case Some(data) => Some(revert(data))
      case _ =>
        logger.error(error)
        None
    }
  }

  protected def findAllByProperty(name: String, value: String, error: String) = {
    val cursor = retrieveCollection.find(MongoDBObject(name -> value))
    if (cursor.isEmpty) {
      logger.error(error)
      Nil
    } else {
      cursor.map(c => revert(c)).toList
    }
  }

  protected def findAll(error: String) = {
    val cursor = retrieveCollection.find()
    if (cursor.isEmpty) {
      logger.error(error)
      Nil
    } else {
      cursor.map(c => revert(c)).toList
    }
  }

  protected def transform(obj: A): MongoDBObject

  protected def revert(obj: MongoDBObject): A

  protected def getInt(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(0).asInstanceOf[Int]

  protected def getString(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse("").asInstanceOf[String]

  protected def getOptional[B](obj: MongoDBObject, name: String) = getVar(obj, name).asInstanceOf[Option[B]]

  protected def getObjectId(obj: MongoDBObject, name: String) = getVar(obj, name).asInstanceOf[Option[ObjectId]].get

  protected def getBigDecimal(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(BigDecimal(0)).asInstanceOf[BigDecimal]

  protected def getDouble(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(0).asInstanceOf[Double]

  protected def getDate(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(new Date).asInstanceOf[Date]

  protected def getBoolean(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(false).asInstanceOf[Boolean]

  protected def getAddress(obj: MongoDBObject, name: String) = {
    val res = getBasicDBList(obj, name).get
    if (res.size() == 5)
      Address(
        res.get(0).asInstanceOf[String],
        Option(res.get(1).asInstanceOf[String]),
        res.get(2).asInstanceOf[String],
        res.get(3).asInstanceOf[String],
        res.get(4).asInstanceOf[String]
      )
    else
      Address(
        res.get(0).asInstanceOf[String],
        None,
        res.get(1).asInstanceOf[String],
        res.get(2).asInstanceOf[String],
        res.get(3).asInstanceOf[String]
      )
  }

  protected def getPassport(obj: MongoDBObject, name: String) = {
    val res = getBasicDBList(obj, name).get
    Passport(
      res.get(0).asInstanceOf[Int],
      res.get(1).asInstanceOf[String],
      res.get(2).asInstanceOf[String]
    )
  }

  protected def getRoarType(obj: MongoDBObject, name: String) = {
    val res = getBasicDBList(obj, name).get
    val roarType = RoarType(
      res.get(0).asInstanceOf[String]
    )
    if (res.size() == 2)
      roarType.copy(limit = res.get(1).asInstanceOf[Option[Int]])
    else
      roarType
  }

  protected def getObject[B](obj: MongoDBObject, name: String) = getVar(obj, name).get.asInstanceOf[B]

  protected def getBasicDBList(obj: MongoDBObject, name: String) = getVar(obj, name).asInstanceOf[Option[BasicDBList]]

  protected def getLong(obj: MongoDBObject, name: String) = getVar(obj, name).getOrElse(0L).asInstanceOf[Long]

  protected def getList[B](obj: MongoDBObject, name: String) = {
    getVar(obj, name).getOrElse(Nil).asInstanceOf[BasicDBList].toList.asInstanceOf[List[B]]
  }

  private def getVar(obj: MongoDBObject, name: String) = obj.get(name)
}
