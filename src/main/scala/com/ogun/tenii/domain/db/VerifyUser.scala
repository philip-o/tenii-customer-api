package com.ogun.tenii.domain.db

import org.bson.types.ObjectId

case class VerifyUser(id: Option[ObjectId] = None, verificationUUID: String, verified: Boolean, timestamp: Long, userId: String)

case class Verified(id: Option[String] = None, timestamp: Long, userId: String)
