package com.ogun.tenii.routes

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.{ CircuitBreaker, ask }
import akka.util.Timeout
import com.ogun.tenii.actors.PasswordActor
import com.ogun.tenii.domain.api.{ PasswordResetRequest, PasswordResetResponse }
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

@Path("/passwordReset")
class PasswordResetRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends Directives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout = Timeout(20 seconds)
  protected val passwordActor = system.actorOf(Props(classOf[PasswordActor]))

  def route = pathPrefix("passwordReset") {
    passwordReset
  }

  def passwordReset: Route =
    post {
      entity(as[PasswordResetRequest]) { request =>
        logger.info(s"POST /passwordReset - $request")
        onCompleteWithBreaker(breaker)(passwordActor ? request) {
          case Success(msg: PasswordResetResponse) => complete(StatusCodes.Created -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
