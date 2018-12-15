package com.ogun.tenii.domain.api

case class LoginRequest(email: String, password: String, ipAddress: String)

case class LoginResponse(success: Boolean, id: Option[String] = None, errorCode: Option[String] = None)