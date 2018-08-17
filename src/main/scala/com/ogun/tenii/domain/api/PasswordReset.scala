package com.ogun.tenii.domain.api

case class PasswordResetRequest(username: String, ipAddress: String)

case class PasswordResetResponse(success: Boolean, errorCode: Option[String], password: Option[String])
