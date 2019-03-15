package com.ogun.tenii.config

import com.typesafe.config.{Config, ConfigFactory}

object Settings {

  private[config] val config: Config = ConfigFactory.load()

  val database = config.getStringList("mongo.database").get(0)
  val host = config.getStringList("mongo.host").get(0)

  val paymentsHost = config.getStringList("tenii.payments.endpoint").get(0)
  val productsHost = config.getStringList("tenii.products.endpoint").get(0)
  val trulayerHost = config.getStringList("tenii.trulayer.endpoint").get(0)

  val createPot = config.getStringList("tenii.payments.createPotRoute").get(0)
  val trulayerLogin = config.getStringList("tenii.trulayer.loginRoute").get(0)
  val trulayerAddUserToCache = config.getStringList("tenii.trulayer.cacheUserRoute").get(0)
  val trulayerAuthURL = config.getStringList("trulayer.authEndpoint").get(0)

  val trulayerRequestedPermissions = config.getStringList("trulayer.permissionsParams").get(0)
  val trulayerClientId = config.getStringList("trulayer.clientId").get(0)
}
