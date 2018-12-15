package com.ogun.tenii.domain.api

case class LoginRequest(email: String, password: String, ipAddress: String)

case class LoginResponse(accounts: List[TrulayerAccount] = Nil, errorCode: Option[String] = None)