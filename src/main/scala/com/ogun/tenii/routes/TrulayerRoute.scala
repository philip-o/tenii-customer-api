package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.TrulayerActor
import com.ogun.tenii.domain.api.TrulayerRegisterRequest
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("register")
@Api(value = "/register", description = "Register endpoint", produces = "application/json")
class TrulayerRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val trulayerActor: ActorRef = system.actorOf(Props(classOf[TrulayerActor]))

  def route: Route = pathPrefix("register") {
    register
  }

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[TrulayerRegisterRequest],
    value = "Register a new tenii user",
    consumes = "application/json",
    notes =
      """
         Register a new tenii user
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "title", dataType = "string", value = "The title for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "forename", dataType = "string", value = "The first name for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "surname", dataType = "string", value = "The surname for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "dob", dataType = "string", value = "The date of birth for the user(YYYYMMDD)", paramType = "body", required = true),
    new ApiImplicitParam(name = "mobile", dataType = "string", value = "The mobile number for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "email", dataType = "string", value = "The email address for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "password", dataType = "string", paramType = "body", value = "The password for the user", required = true),
    new ApiImplicitParam(name = "roarType", dataType = "com.ogun.tenii.domain.common.RoarType", value = "The roar type for the user", paramType = "body", required = true),
    new ApiImplicitParam(name = "ipAddress", dataType = "string", paramType = "body", value = "The ip address for the user's client", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[String]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def register: Route = {
    post {
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