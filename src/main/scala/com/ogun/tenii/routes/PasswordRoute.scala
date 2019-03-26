package com.ogun.tenii.routes

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.PasswordActor
import com.ogun.tenii.domain.api._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("/password")
@Api(value = "/password", description = "Password change/update route", produces = "application/json")
class PasswordRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout = Timeout(20 seconds)
  protected val passwordActor = system.actorOf(Props(classOf[PasswordActor]))

  def route = pathPrefix("password") {
    passwordReset ~ updatePassword
  }

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[PasswordResetResponse],
    value = "Reset a user's password",
    consumes = "application/json",
    notes =
      """
         Reset a user's password
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "email", dataType = "string", value = "The email address for user", paramType = "body", required = true),
    new ApiImplicitParam(name = "ipAddress", dataType = "string", paramType = "body", value = "The ip address for the user's client", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[PasswordResetResponse]),
    new ApiResponse(code = 404, message = "Not Found", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def passwordReset: Route =
    post {
      entity(as[PasswordResetRequest]) { request =>
        logger.info(s"POST /password reset - $request")
        onCompleteWithBreaker(breaker)(passwordActor ? request) {
          case Success(msg: PasswordResetResponse) => complete(StatusCodes.OK -> msg)
          case Success(msg: ErrorResponse) => complete(StatusCodes.NotFound -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }

  @Path("update/{email}")
  @ApiOperation(
    httpMethod = "POST",
    response = classOf[PasswordResetResponse],
    value = "Update a user's password",
    consumes = "application/json",
    notes =
      """
         Update a user's password
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "email", dataType = "string", value = "The email address for user", paramType = "path", required = true),
    new ApiImplicitParam(name = "userInfo", dataType = "com.ogun.tenii.domain.api.PasswordUserInfo", paramType = "body", value = "The ip address for the user's client, old and new password", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[PasswordResetResponse]),
    new ApiResponse(code = 404, message = "Not Found", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def updatePassword: Route =
    post {
      (path("update" ) & emailDirective & entity(as[PasswordUserInfo])).as(PasswordChangeRequest) { request =>
        logger.info(s"POST /password/update reset  - $request")
        onCompleteWithBreaker(breaker)(passwordActor ? request) {
          case Success(msg: PasswordResetResponse) => complete(StatusCodes.OK -> msg)
          case Success(msg: ErrorResponse) => complete(StatusCodes.NotFound -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
