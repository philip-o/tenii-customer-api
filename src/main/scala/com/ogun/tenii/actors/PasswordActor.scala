package com.ogun.tenii.actors

import akka.actor.{Actor, ActorRef, Props}
import com.ogun.tenii.db.UserConnection
import com.ogun.tenii.domain.api
import com.ogun.tenii.domain.api.{ErrorResponse, PasswordResetRequest, PasswordResetResponse}
import com.ogun.tenii.domain.password.{PasswordUserLookupRequest, PasswordUserLookupResponse}
import com.ogun.tenii.util.PasswordUtil
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PasswordActor extends Actor with LazyLogging {

  protected val userActor: ActorRef = context.actorOf(Props[UserActor])
  private val userConnection = new UserConnection

  override def receive: Receive = {
    case req: PasswordResetRequest =>
      //TODO
      //Check when user last logged in?
      //Return password if valid user or reject and log ip address
      val senderRef = sender()
      userActor ! PasswordUserLookupRequest(sender(), req)
      Future {
        userConnection.findByEmail(req.email)
      } onComplete {
        case Success(resp) => resp match {
          case Some(user) => val password = PasswordUtil.createPassword()
            Future {
            userConnection.save(user.copy(password = password))
          } onComplete {
            case Success(_) => senderRef ! PasswordResetResponse(success = true, None, Some(password))
            //TODO Send email to user with new password
            case Failure(t) => logger.error(s"Error thrown when trying to save update: $user", t)
              senderRef ! ErrorResponse("PASSWORD_SAVE_FAILED", Some(s"Error thrown due to $t"))
          }
          case None => senderRef ! ErrorResponse("USER_NOT_FOUND", None)
        }
        case Failure(t) => logger.error(s"Could not find the user due to an error", t)
          senderRef ! api.ErrorResponse("SEARCH_ERROR")
      }

    case resp: PasswordUserLookupResponse =>
      val password = "password"
      resp.user match {
        case Some(user) => Future {
          userConnection.save(user.copy(password = password))
        } onComplete {
          case Success(_) => resp.actorRef ! PasswordResetResponse(success = true, None, Some(password))
          //TODO Send email to user with new password
          case Failure(t) => resp.actorRef ! PasswordResetResponse(success = false, Some("PASSWORD_SAVE_FAILED"), None)
        }
        case None => resp.actorRef ! PasswordResetResponse(success = false, Some("USER_NOT_FOUND"), None)
      }

    case other => logger.error(s"Received unknown message, please check: $other")
  }
}
