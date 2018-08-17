package com.ogun.tenii.db

import com.mongodb.casbah.Imports._
import com.ogun.tenii.domain.db.VerifyUser
import com.typesafe.scalalogging.LazyLogging

class VerifyUserConnection extends ObjectMongoConnection[VerifyUser] with LazyLogging {

  val collection = "verify"

  override def transform(obj: VerifyUser): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "verificationUUID" -> obj.verificationUUID, "verified" -> obj.verified,
      "timestamp" -> obj.timestamp, "userId" -> obj.userId)
  }

  def findByUserId(userId: String): Option[VerifyUser] = {
    findByProperty("userId", userId, s"No user found with userId: $userId")
  }

  def findByVerificationId(verificationUUID: String): Option[VerifyUser] = {
    findByProperty("verificationUUID", verificationUUID, s"No verification found with verificationUUID: $verificationUUID")
  }

  def findById(id: String): Option[VerifyUser] =
    findByObjectId(id, s"No verification found with id: $id")

  override def revert(obj: MongoDBObject): VerifyUser = {
    VerifyUser(
      Some(getObjectId(obj, "_id")),
      getString(obj, "verificationUUID"),
      getBoolean(obj, "verified"),
      getLong(obj, "timestamp"),
      getString(obj, "userId")
    )
  }
}
