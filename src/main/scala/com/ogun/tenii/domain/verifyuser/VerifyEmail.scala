package com.ogun.tenii.domain.verifyuser

import org.bson.types.ObjectId

case class VerifyEmailPersistRequest(userId: ObjectId, email: String)

