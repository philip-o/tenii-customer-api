package com.ogun.tenii.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ogun.tenii.db.UserConnection
import com.ogun.tenii.domain.api.{TrulayerAccessToken, TrulayerAccessTokenResponse, TrulayerRegisterRequest}
import com.ogun.tenii.external.HttpTransfers
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success}

class TrulayerActor extends Actor with LazyLogging with TrulayerEndpoint {

  private val connection = new UserConnection

  implicit val system: ActorSystem = context.system
  val http = new HttpTransfers()
  val clientId: String = Properties.envOrElse("CLIENT_ID", "blabla")
  val userActor: ActorRef = system.actorOf(Props(classOf[UserActor]))
  implicit val timeout: FiniteDuration = 10.seconds

  override def receive: Receive = {
    case request: TrulayerRegisterRequest =>
      val senderRef = sender()
      implicit val timeout: Timeout = Timeout(10.seconds)
      (userActor ? request) onComplete {
        case Success(_) => senderRef ! s"$trulayerHost$responseType&$clientIdParam$clientId&$nonceParam${UUID.randomUUID().toString}&$permissionsParam"
        case Failure(t) => logger.error(s"Error thrown when attempting to register user", t)
      }
    case request: TrulayerAccessToken =>
      val senderRef = sender()
      Future {
        connection.findByNoAccessToken()
      } onComplete {
        case Success(userOpt) => userOpt match {
          case Some(user) => Future {
            connection.save(user.copy(accessToken = Some(request.accessToken), refreshToken = Some(request.refreshToken)))
          } onComplete {
            case Success(_) => senderRef ! TrulayerAccessTokenResponse()
            case Failure(t) => logger.error(s"Error thrown when saving user, please check: $request", t)
              senderRef ! TrulayerAccessTokenResponse(Some("Error thrown when saving user"))
          }
          case None => logger.error(s"Did not find a user with no token, please check: $request")
            senderRef ! TrulayerAccessTokenResponse(Some("Did not find a user with no token"))
        }
        case Failure(t) => logger.error(s"Error thrown when looking for user, please check: $request", t)
          senderRef ! TrulayerAccessTokenResponse(Some("Error thrown when looking for user"))
      }
    case other => logger.error(s"Unknown message received: $other")
  }
}

trait TrulayerEndpoint {

  val trulayerHost = "https://auth.truelayer.com/?"
  val responseType = "response_type=code"
  val clientIdParam = "client_id="
  val nonceParam = "nonce="
  val permissionsParam = "scope=info%20accounts%20balance%20transactions%20cards%20offline_access&redirect_uri=https://tenii-demo.herokuapp.com/postauth&enable_mock=true&enable_oauth_providers=true&enable_open_banking_providers=false&enable_credentials_sharing_providers=true"
}
