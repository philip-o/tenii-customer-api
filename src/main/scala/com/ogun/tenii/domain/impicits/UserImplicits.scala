package com.ogun.tenii.domain.impicits

import java.util.UUID

import com.ogun.tenii.domain.api.TrulayerRegisterRequest
import com.ogun.tenii.domain.common.{Address, Passport}
import com.ogun.tenii.domain.db.{User, VerifyUser}
import com.ogun.tenii.domain.verifyuser.VerifyEmailPersistRequest

trait UserImplicits {

  implicit def toUser(request: TrulayerRegisterRequest): User = {
    //TODO Add logic to determine bank and persist so adding other banks can filter
    User(
      title = request.title,
      forename = request.forename,
      surname = request.surname,
      address = Address(
        address1 = "address1",
        None,
        city = "London",
        postCode = "",
        country = "UK"
      ),
      dob = request.dob,
      username = UUID.randomUUID().toString,
      password = request.password,
      mobile = request.mobile,
      identification = Passport(
        12345678,
        "UK",
        "20201231"
      ),
      ipAddress = request.ipAddress,
      email = request.email,
      roarType = request.roarType,
      provider = request.provider
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
