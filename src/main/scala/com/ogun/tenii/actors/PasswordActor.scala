package com.ogun.tenii.actors

import akka.actor.Actor
import com.ogun.tenii.db.UserConnection
import com.ogun.tenii.domain.api.{ErrorResponse, PasswordChangeRequest, PasswordResetRequest, PasswordResetResponse}
import com.ogun.tenii.util.PasswordUtil
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PasswordActor extends Actor with LazyLogging {

  private val userConnection = new UserConnection

  override def receive: Receive = {
    case req: PasswordResetRequest =>
      val senderRef = sender()
      Future {
        userConnection.findByEmail(req.email)
      } onComplete {
        case Success(resp) => resp match {
          case Some(user) => val password = PasswordUtil.createPassword()
            Future {
            userConnection.save(user.copy(password = password))
          } onComplete {
            case Success(_) => senderRef ! PasswordResetResponse(password)
            //TODO Send email to user with new password
            case Failure(t) => logger.error(s"Error thrown when trying to save update: $user", t)
              senderRef ! ErrorResponse("PASSWORD_SAVE_FAILED", Some(s"Error thrown due to $t"))
          }
          case None => senderRef ! ErrorResponse("USER_NOT_FOUND")
            logger.error(s"User not found for request: $req")
        }
        case Failure(t) => logger.error(s"Could not find the user due to an error", t)
          senderRef ! ErrorResponse("SEARCH_ERROR")
      }

    case req: PasswordChangeRequest =>
      val senderRef = sender()
      Future {
        userConnection.findByEmail(req.email)
      } onComplete {
        case Success(userOpt) => userOpt match {
          case Some(user) => if(user.password != req.userInfo.oldPassword) {
              senderRef ! ErrorResponse("INVALID_PREVIOUS_PASSWORD")
              logger.error(s"User's old password is inccorect: $req")
            }
          else {
            Future {
              userConnection.save(user.copy(password = req.userInfo.newPassword))
            } onComplete {
              case Success(_) => senderRef ! PasswordResetResponse(req.userInfo.newPassword)
              case Failure(t) => senderRef ! ErrorResponse("PASSWORD_SAVE_FAILED", Some(s"Error thrown due to $t"))
                logger.error(s"Error thrown when trying to save update: $user", t)
            }
          }
          case None => senderRef ! ErrorResponse("USER_NOT_FOUND")
            logger.error(s"User not found for request: $req")
        }
        case Failure(t) => logger.error(s"Error thrown when trying to update: $req", t)
          senderRef ! ErrorResponse("PASSWORD_SAVE_FAILED", Some(s"Error thrown due to $t"))
      }
    case other => logger.error(s"Received unknown message, please check: $other")
  }
}
