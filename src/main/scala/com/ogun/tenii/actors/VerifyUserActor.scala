package com.ogun.tenii.actors

import akka.actor.Actor
import com.ogun.tenii.db.VerifyUserConnection
import com.ogun.tenii.domain.api.{ VerifyAccountRequest, VerifyAccountResponse }
import com.ogun.tenii.domain.impicits.UserImplicits
import com.ogun.tenii.domain.verifyuser.VerifyEmailPersistRequest
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class VerifyUserActor extends Actor with LazyLogging with UserImplicits {

  private val connection = new VerifyUserConnection

  override def receive: Receive = {
    case request: VerifyEmailPersistRequest =>
      Future {
        connection.save(request)
      } onComplete {
        case Success(_) => connection.findByUserId(request.userId.toString) match {
          case Some(_) => //TODO Send email to user with verify link
          case None => logger.error(s"Unable to save find verification entry $request check urgently and send email")
        }
        case Failure(t) => logger.error(s"Unable to save verification request $request check urgently and send email", t)
      }

    case request: VerifyAccountRequest =>
      val senderRef = sender()
      Future {
        connection.findByVerificationId(request.verificationUUID)
      } onComplete {
        case Success(entry) => entry match {
          case Some(e) => Future {
            connection.save(e.copy(verified = true))
          } onComplete {
            case Success(_) => senderRef ! VerifyAccountResponse(status = true, None)
            case Failure(t) =>
              logger.error("Unable to save, please check urgently", t)
              senderRef ! VerifyAccountResponse(status = false, Some("SAVE_FAILURE"))
          }
          case None =>
            logger.error("Unable to save, please check urgently, cannot find entry, maybe no longer valid")
            senderRef ! VerifyAccountResponse(status = false, Some("NO_USER_ERROR"))
        }
        case Failure(t) => logger.error("Unable to find user, please check urgently", t)
          senderRef ! VerifyAccountResponse(status = false, Some("SEARCH_ERROR"))
      }
  }
}
