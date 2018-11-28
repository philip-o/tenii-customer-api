package com.ogun.tenii.domain.impicits

import java.util.UUID

import com.ogun.tenii.domain.api.{ RegisterRequest, TellerRegisterRequest, TrulayerRegisterRequest }
import com.ogun.tenii.domain.common.{ Address, Passport }
import com.ogun.tenii.domain.db.{ TellerUser, User, VerifyUser }
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
      roarType = request.roarType,
      accessToken = None,
      refreshToken = None
    )
  }

  implicit def toUser(request: TrulayerRegisterRequest): User = {
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
      accessToken = None,
      refreshToken = None
    )
  }

  implicit def toTellerUser(request: TellerRegisterRequest): TellerUser = {
    TellerUser(
      title = request.title,
      forename = request.forename,
      surname = request.surname,
      dob = request.dob,
      password = request.password,
      mobile = request.mobile,
      email = request.email,
      roarType = request.roarType,
      ipAddress = request.ipAddress
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
