package com.ogun.tenii.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.actors.TellerActor
import com.ogun.tenii.domain.api._
import com.ogun.tenii.domain.teller.TellerResponse
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("/tellerLogin")
class TellerLoginRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val tellerActor: ActorRef = system.actorOf(Props(classOf[TellerActor]))

  def route: Route = pathPrefix("tellerLogin") {
    teller
  }

  def teller: Route = {
    post {
      entity(as[TellerLoginRequest]) { request =>
        logger.info(s"POST login - $request")
        onCompleteWithBreaker(breaker)(tellerActor ? request) {
          case Success(msg: List[TellerResponse]) => complete(StatusCodes.OK -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
  }
}
