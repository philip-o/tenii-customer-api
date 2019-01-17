package com.ogun.tenii.routes

import akka.http.scaladsl.server.Route
import com.ogun.tenii.actors.UserActor
import com.ogun.tenii.domain.api.{RegisterRequest, RegisterResponse}
import javax.ws.rs.Path
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations.Api

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/register")
@Api(value = "/register", description = "Register endpoint", produces = "application/json")
class RegisterRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val userActor: ActorRef = system.actorOf(Props(classOf[UserActor]))

  def route: Route = pathPrefix("register") {
    register
  }

  def register: Route =
    post {
      entity(as[RegisterRequest]) { request =>
        logger.info(s"POST /register - $request")
        onCompleteWithBreaker(breaker)(userActor ? request) {
          case Success(msg: RegisterResponse) => complete(StatusCodes.Created -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
