package com.ogun.tenii.domain.api

import com.ogun.tenii.domain.common.RoarType

case class TrulayerRegisterRequest(title: String = "Mr", forename: String, surname: String, dob: String,
                                 password: String, mobile: String, email: String, roarType: RoarType, ipAddress: String)
