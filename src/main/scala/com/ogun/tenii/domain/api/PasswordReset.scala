package com.ogun.tenii.domain.api

case class PasswordResetRequest(email: String, ipAddress: String)

case class PasswordResetResponse(password: String)

case class PasswordUserInfo(ipAddress: String, oldPassword: String, newPassword: String)

case class PasswordChangeRequest(email: String, userInfo: PasswordUserInfo)

