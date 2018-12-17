package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.UserActor
import com.ogun.tenii.domain.api.{LoginRequest, LoginResponse, TrulayerAccountsResponse}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import javax.ws.rs.Path

@Path("/login")
class LoginRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(30.seconds)
  protected val userActor: ActorRef = system.actorOf(Props(classOf[UserActor]))

  def route: Route = pathPrefix("login") {
    login
  }

  def login: Route = {
    post {
      entity(as[LoginRequest]) { request =>
        logger.info(s"POST /login - $request")
        onCompleteWithBreaker(breaker)(userActor ? request) {
          case Success(msg: LoginResponse) if msg.errorCode.nonEmpty => complete(StatusCodes.InternalServerError -> msg)
          case Success(msg: LoginResponse) => complete(StatusCodes.OK -> TrulayerAccountsResponse(accounts = msg.accounts, teniiId = msg.teniiId))
          case Failure(t) => failWith(t)
        }
      }
    }
  }
}
