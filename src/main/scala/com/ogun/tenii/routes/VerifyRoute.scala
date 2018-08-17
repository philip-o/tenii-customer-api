package com.ogun.tenii.routes

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{ ask, CircuitBreaker }
import akka.util.Timeout
import com.ogun.tenii.actors.VerifyUserActor
import com.ogun.tenii.domain.api.{ VerifyAccountRequest, VerifyAccountResponse }
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
import javax.ws.rs.Path

@Path("/verify")
class VerifyRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val verifyActor: ActorRef = system.actorOf(Props(classOf[VerifyUserActor]))

  def route: Route = pathPrefix("verify") {
    verify
  }

  def verify: Route = {
    post {
      entity(as[VerifyAccountRequest]) { request =>
        logger.info(s"POST /verify - $request")
        onCompleteWithBreaker(breaker)(verifyActor ? request) {
          case Success(msg: VerifyAccountResponse) => complete(StatusCodes.OK -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
  }
}
