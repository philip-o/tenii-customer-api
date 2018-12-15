package com.ogun.tenii.external

trait TeniiEndpoints {

  val paymentsApiHost = "https://tenii-payments-api.herokuapp.com/"
  val productsApiHost = "https://tenii-products-api.herokuapp.com/"
  val trulayerApiHost = "https://tenii-trulayer-api.herokuapp.com/"
  val createPot = "teller/createPot/"
  val bankAccounts = "teller/bankAccounts"
  val login = "login"

  implicit def onSuccessDecodingError[TellerResponse](decodingError: io.circe.Error): TellerResponse = throw new Exception(s"Error decoding trains upstream response: $decodingError")
  implicit def onErrorDecodingError[TellerResponse](decodingError: String): TellerResponse = throw new Exception(s"Error decoding upstream error response: $decodingError")
}
