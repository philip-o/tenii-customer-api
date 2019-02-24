package com.ogun.tenii.external

trait TeniiEndpoints {

  val paymentsApiHost = "https://tenii-payments-api.herokuapp.com/"
  val productsApiHost = "https://tenii-products-api.herokuapp.com/"
  val trulayerApiHost = "https://tenii-trulayer-api.herokuapp.com/"
  val createPot = "pot"
  val bankAccounts = "teller/bankAccounts"
  val login = "login"
  val addTeniiId = "newUser"

  implicit def onSuccessDecodingError[Response](decodingError: io.circe.Error): Response = throw new Exception(s"Error decoding upstream response: $decodingError")
  implicit def onErrorDecodingError[Response](decodingError: String): Response = throw new Exception(s"Error decoding upstream error response: $decodingError")
}
