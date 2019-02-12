package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.VerifyUserActor
import com.ogun.tenii.domain.api.{ErrorResponse, VerifyAccountRequest, VerifyAccountResponse}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import javax.ws.rs.Path

@Path("/verify")
@Api(value = "/verify", description = "Verify user account", produces = "application/json")
class VerifyRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val verifyActor: ActorRef = system.actorOf(Props(classOf[VerifyUserActor]))

  def route: Route = pathPrefix("verify") {
    verify
  }

  //TODO Update account creation to send email
  @ApiOperation(
    httpMethod = "POST",
    response = classOf[VerifyAccountResponse],
    value = "Verify a user's account",
    consumes = "application/json",
    notes =
      """
         Verify a user's account
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "verificationUUID", dataType = "string", value = "The verification id for user", paramType = "body", required = true),
    new ApiImplicitParam(name = "ipAddress", dataType = "string", paramType = "body", value = "The ip address for the user's client", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[VerifyAccountResponse]),
    new ApiResponse(code = 400, message = "Bad Request", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def verify: Route = {
    post {
      entity(as[VerifyAccountRequest]) { request =>
        logger.info(s"POST /verify - $request")
        onCompleteWithBreaker(breaker)(verifyActor ? request) {
          case Success(msg: VerifyAccountResponse) if !msg.status => complete(StatusCodes.BadRequest -> ErrorResponse(msg.reason.get))
          case Success(msg: VerifyAccountResponse) => complete(StatusCodes.OK -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
  }
}
