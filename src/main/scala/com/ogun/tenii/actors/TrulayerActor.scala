package com.ogun.tenii.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ogun.tenii.domain.api.TrulayerRegisterRequest
import com.ogun.tenii.external.HttpTransfers
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success}

class TrulayerActor extends Actor with LazyLogging with TrulayerEndpoint {

  implicit val system : ActorSystem = context.system
  val http = new HttpTransfers()
  val clientId : String = Properties.envOrElse("CLIENT_ID", "blabla")
  val userActor : ActorRef = system.actorOf(Props(classOf[UserActor]))
  implicit val timeout : FiniteDuration = 10.seconds

  override def receive: Receive = {
    case request : TrulayerRegisterRequest =>
      val senderRef = sender()
      implicit val timeout: Timeout = Timeout(10.seconds)
      (userActor ? request) onComplete {
        case Success(_) => senderRef ! s"$trulayerHost$responseType&$clientIdParam$clientId&$tellerAppId&permissions=$permissions"
        case Failure(t) => logger.error(s"Error thrown when attempting to register user", t)
      }

    case other => logger.error(s"Unknown message received: $other")
  }

}

trait TrulayerEndpoint {

  val trulayerHost = "https://auth.truelayer.com/?"
  val responseType = "response_type=code"
  val clientIdParam = "client_id="

  //https://auth.truelayer.com/?response_type=code&client_id=tenii-gvem&nonce=287206509&scope=info%20accounts%20balance%20transactions%20cards%20offline_access&redirect_uri=https://console.truelayer.com/redirect-page&enable_mock=true&enable_oauth_providers=true&enable_open_banking_providers=false&enable_credentials_sharing_providers=true
}
