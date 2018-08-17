package com.ogun.tenii.domain.api

case class VerifyAccountRequest(verificationUUID: String, ipAddress: String)

case class VerifyAccountResponse(status: Boolean, reason: Option[String])