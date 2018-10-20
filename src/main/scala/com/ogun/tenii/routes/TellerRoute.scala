package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.TellerActor
import com.ogun.tenii.domain.api._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/teller")
class TellerRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val tellerActor: ActorRef = system.actorOf(Props(classOf[TellerActor]))

  def route: Route = pathPrefix("teller") {
    tellerPostAuth ~ register
  }

  def register: Route = {
      post {
        path("register") {
          entity(as[TellerRegisterRequest]) { request =>
            logger.info(s"POST register - $request")
            onCompleteWithBreaker(breaker)(tellerActor ? request) {
              case Success(msg: String) => logger.info(s"URL is $msg")
                complete(StatusCodes.OK -> msg)
              case Failure(t) => failWith(t)
            }
          }
        }
      }
  }

  def tellerPostAuth: Route = {
    get {
      (path("postauth") & accessTokenDirective & permissionsDirective).as(TellerAPIPermissionsResponse) { request =>
        logger.info(s"GET teller/postauth - $request")
        onCompleteWithBreaker(breaker)(tellerActor ? request) {
          case Success(msg: TellerPermissionsResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg)
          case Success(msg: TellerPermissionsResponse) => complete(StatusCodes.NotAcceptable -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
  }
}
