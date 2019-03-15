package com.ogun.tenii.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ogun.tenii.config.Settings
import com.ogun.tenii.domain.api._
import com.ogun.tenii.external.HttpTransfers
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TrulayerActor extends Actor with LazyLogging with TrulayerEndpoint {

  implicit val system: ActorSystem = context.system
  val http = new HttpTransfers()
  val clientId: String = Settings.trulayerClientId
  val userActor: ActorRef = system.actorOf(Props(classOf[UserActor]))
  implicit val timeout: FiniteDuration = 10.seconds

  override def receive: Receive = {
    case request: TrulayerRegisterRequest =>
      val senderRef = sender()
      implicit val timeout: Timeout = Timeout(10.seconds)
      (userActor ? request) onComplete {
        case Success(resp: RegisterResponse) if !resp.success => senderRef ! ErrorResponse("NO_USER_ERROR")
        case Success(_) => senderRef ! s"$trulayerHost$responseType&$clientIdParam$clientId&$nonceParam${UUID.randomUUID().toString}&$permissionsParam"
        case Failure(t) => logger.error(s"Error thrown when attempting to register user", t)
      }
    case other => logger.error(s"Unknown message received: $other")
  }
}

trait TrulayerEndpoint {

  val trulayerHost = Settings.trulayerAuthURL
  val responseType = "response_type=code"
  val clientIdParam = "client_id="
  val nonceParam = "nonce="
  val permissionsParam = Settings.trulayerRequestedPermissions
}
