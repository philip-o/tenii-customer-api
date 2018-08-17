package com.ogun.tenii.domain.api

import com.ogun.tenii.domain.common.{ Address, Passport, RoarType, TeniiDate }

case class RegisterRequest(title: String = "Mr", forename: String, middle: Option[String] = None, surname: String, address: Address, dob: String, username: String,
  password: String, mobile: String, id: Passport, ipAddress: String, email: String, roarType: RoarType)

case class RegisterResponse(email: String, mobile: String, success: Boolean = true, error: Option[String] = None)