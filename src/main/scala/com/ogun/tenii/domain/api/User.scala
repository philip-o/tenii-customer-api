package com.ogun.tenii.domain.api

case class RegisterResponse(email: String, mobile: String, success: Boolean = true, error: Option[String] = None)