package com.ogun.tenii.domain.api

import com.ogun.tenii.domain.common.RoarType

case class TrulayerRegisterRequest(title: String = "Mr", forename: String, surname: String, dob: String,
  password: String, mobile: String, email: String, roarType: RoarType, ipAddress: String)

case class TrulayerAccessToken(accessToken: String, refreshToken: String)

case class TrulayerAccessTokenResponse(errorCode: Option[String] = None)

case class Provider(display_name: String, logo_uri: String, provider_id: String)

case class AccountNumbers(number: String, sort_code: String)

case class TrulayerAccount(account_id: String, account_type: String, account_number: AccountNumbers, currency: String, provider: Provider, balance: Double)

case class TrulayerAccountsResponse(accounts: List[TrulayerAccount], accessToken: String = "", refreshToken: String = "", error: Option[String] = None)

case class TrulayerLoginRequest(teniiId: String)