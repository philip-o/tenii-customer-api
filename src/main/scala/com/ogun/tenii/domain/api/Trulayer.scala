package com.ogun.tenii.domain.api

import com.ogun.tenii.domain.common.RoarType

case class TrulayerRegisterRequest(title: String = "Mr", forename: String, surname: String, dob: String,
  password: String, mobile: String, email: String, roarType: RoarType, ipAddress: String)

case class TrulayerAccessToken(accessToken: String, refreshToken: String)

case class TrulayerAccessTokenResponse(errorCode: Option[String] = None)

case class Provider(display_name: String, logo_uri: String, provider_id: String)

case class AccountNumbers(number: String, sort_code: String)

case class TrulayerAccount(account_id: String, account_type: String, account_number: AccountNumbers, currency: String, provider: Provider, balance: Double)

case class TrulayerAccountsResponse(accounts: List[TrulayerAccount], accessToken: String = "", refreshToken: String = "", error: Option[String] = None, teniiId: Option[String] = None)

case class TrulayerAddUserRequest(teniiId: String)

case class Status(status: String)

case class ThirdPartyAddUserAndBankRequest(teniiId: String, provider: String)

case class UserAccountRequest(teniiId: String, provider: String)

case class TrulayerRegisterRequestV2(title: String = "Mr", forename: String, surname: String, dob: String,
                                     password: String, mobile: String, email: String, roarType: RoarType,
                                     ipAddress: String, provider: String)

object Providers extends Enumeration {
  val Mock, Halifax, Natwest, Lloyds, HSBC, Barclays, Nationwide, Amex, FirstDirect, TSB, BarclayCard, RBS, COOP,
  LloydsBusiness, BankOfScotland, Metro, Santander, Monzo, Starling = Value
}