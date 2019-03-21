package com.ogun.tenii.domain.api

case class PasswordResetRequest(email: String, ipAddress: String)

case class PasswordResetResponse(password: String)
