package com.ogun.tenii.domain.api

import com.ogun.tenii.domain.common.RoarType

case class TellerPermissions(permission: String, allowed: Boolean)
case class TellerAPIPermissionsResponse(token: String, permissions: List[TellerPermissions])
case class TellerPermissionsResponse(token: Option[String], cause: Option[String] = None)

case class TellerRegisterRequest(title: String = "Mr", forename: String, surname: String, dob: String,
                           password: String, mobile: String, email: String, roarType: RoarType, ipAddress: String)

case class TellerTeniiPotCreateRequest(tellerUserId: String, limit: Double)

case class TellerTeniiPotCreateResponse(tellerUserId: String, cause: Option[String] = None)

case class TellerLoginRequest(email: String, password: String, ipAddress: String)

case class TellerLoginResponse(token: String, cause: Option[String] = None)