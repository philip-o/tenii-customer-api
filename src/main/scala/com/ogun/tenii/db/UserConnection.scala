package com.ogun.tenii.db

import com.mongodb.casbah.Imports._
import com.ogun.tenii.domain.db.User
import com.typesafe.scalalogging.LazyLogging

class UserConnection extends ObjectMongoConnection[User] with LazyLogging {

  val collection = "users"

  override def transform(obj: User): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "title" -> obj.title, "forename" -> obj.forename, "middle" -> obj.middle, "surname" -> obj.surname, "address" -> obj.address,
      "dob" -> obj.dob, "username" -> obj.username, "password" -> obj.password, "mobile" -> obj.mobile, "identification" -> obj.identification,
      "ipAddress" -> obj.ipAddress, "email" -> obj.email, "roarType" -> obj.roarType, "provider" -> obj.provider)
  }

  def findByUsername(name: String): Option[User] = {
    findByProperty("username", name, s"No user found with username: $name")
  }

  def findByEmail(email: String): Option[User] = {
    findByProperty("email", email, s"No user found with email: $email")
  }

  def findByMobile(mobile: String): Option[User] = {
    findByProperty("mobile", mobile, s"No user found with mobile: $mobile")
  }

  def findById(id: String): Option[User] = findByObjectId(id, s"No user found with id: $id")

  override def revert(obj: MongoDBObject): User = {
    User(
      Some(getObjectId(obj, "_id")),
      getString(obj, "title"),
      getString(obj, "forename"),
      getOptional[String](obj, "middle"),
      getString(obj, "surname"),
      getAddress(obj, "address"),
      getString(obj, "dob"),
      getString(obj, "username"),
      getString(obj, "password"),
      getString(obj, "mobile"),
      getPassport(obj, "identification"),
      getString(obj, "ipAddress"),
      getString(obj, "email"),
      getRoarType(obj, "roarType"),
      getString(obj, "provider")
    )
  }
}
