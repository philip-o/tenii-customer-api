package com.ogun.tenii.domain.api

case class LoginRequest(username: String, password: String, ipAddress: String)

case class LoginResponse(success: Boolean, errorCode: Option[String] = None)