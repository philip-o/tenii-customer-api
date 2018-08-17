package com.ogun.tenii.domain.impicits

import java.util.UUID

import com.ogun.tenii.domain.api.RegisterRequest
import com.ogun.tenii.domain.db.{ User, VerifyUser }
import com.ogun.tenii.domain.verifyuser.VerifyEmailPersistRequest

trait UserImplicits {

  implicit def toUser(request: RegisterRequest): User = {
    User(
      title = request.title,
      forename = request.forename,
      middle = request.middle,
      surname = request.surname,
      address = request.address,
      dob = request.dob,
      username = request.username,
      password = request.password,
      mobile = request.mobile,
      identification = request.id,
      ipAddress = request.ipAddress,
      email = request.email,
      roarType = request.roarType
    )
  }

  implicit def toVerifyUser(request: VerifyEmailPersistRequest): VerifyUser = {
    VerifyUser(
      id = None,
      verificationUUID = UUID.randomUUID().toString,
      verified = false,
      timestamp = System.currentTimeMillis(),
      userId = request.userId.toString
    )
  }
}