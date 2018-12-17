package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.TrulayerActor
import com.ogun.tenii.domain.api.{LoginRequest, TrulayerRegisterRequest}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("trulayer")
class TrulayerRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val trulayerActor: ActorRef = system.actorOf(Props(classOf[TrulayerActor]))

  def route: Route = pathPrefix("trulayer") {
    register
  }

  def register: Route = {
    post {
      path("register") {
        entity(as[TrulayerRegisterRequest]) { request =>
          logger.info(s"POST /register - $request")
          onCompleteWithBreaker(breaker)(trulayerActor ? request) {
            case Success(msg: String) =>
              logger.debug(s"URL is $msg")
              complete(StatusCodes.OK -> msg)
            case Failure(t) => failWith(t)
          }
        }
      }
    }
  }
}