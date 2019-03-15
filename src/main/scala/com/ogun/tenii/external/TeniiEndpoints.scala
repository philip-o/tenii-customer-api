package com.ogun.tenii.external

import com.ogun.tenii.config.Settings

trait TeniiEndpoints {

  val paymentsApiHost = Settings.paymentsHost
  val productsApiHost = Settings.productsHost
  val trulayerApiHost = Settings.trulayerHost
  val createPot = Settings.createPot
  val login = Settings.trulayerLogin
  val addTeniiId = Settings.trulayerAddUserToCache

  implicit def onSuccessDecodingError[Response](decodingError: io.circe.Error): Response = throw new Exception(s"Error decoding upstream response: $decodingError")
  implicit def onErrorDecodingError[Response](decodingError: String): Response = throw new Exception(s"Error decoding upstream error response: $decodingError")
}
