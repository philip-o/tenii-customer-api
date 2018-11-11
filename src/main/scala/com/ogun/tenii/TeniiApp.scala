package com.ogun.tenii

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.ogun.tenii.routes._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.util.Properties

object TeniiApp extends App with LazyLogging with RouteConcatenation {

  val applicationName = "tenii-customer"

  implicit val system = ActorSystem(applicationName)
  implicit val mat = ActorMaterializer()

  logger.info(s"Number of processors visible to $applicationName service: ${Runtime.getRuntime.availableProcessors()}")

  implicit val breaker = CircuitBreaker(system.scheduler, 20, 100 seconds, 12 seconds)

  // routes
  //val swaggerDocRoute = new SwaggerDocRoute().routes
  val loginRoute = new LoginRoute().route
  val passwordResetRoute = new PasswordResetRoute().route
  val registerRoute = new RegisterRoute().route
  val verifyRoute = new VerifyRoute().route
  val tellerRoute = new TellerRoute().route
  val tellerLoginRoute = new TellerLoginRoute().route
  val trulayerRoute = new TrulayerRoute().route

  val routes = loginRoute ~ passwordResetRoute ~ registerRoute ~ verifyRoute  ~ tellerRoute ~ tellerLoginRoute ~ trulayerRoute //buildInfoRoute.route ~ healthRoute ~ swaggerDocRoute ~ swaggerSiteRoute ~ prebookingRoute ~ vendorRoute

  val port = Properties.envOrElse("PORT", "8080").toInt
  Http().bindAndHandle(routes, "0.0.0.0", port)
  logger.info(s"$applicationName application started on port $port")

}
