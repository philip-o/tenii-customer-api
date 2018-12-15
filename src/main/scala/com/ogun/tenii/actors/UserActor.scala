package com.ogun.tenii.actors

import akka.actor.{ Actor, ActorRef, Props }
import com.ogun.tenii.db.UserConnection
import com.ogun.tenii.domain.api._
import com.ogun.tenii.domain.impicits.UserImplicits
import com.ogun.tenii.domain.password.{ PasswordUserLookupRequest, PasswordUserLookupResponse }
import com.ogun.tenii.domain.verifyuser.VerifyEmailPersistRequest
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class UserActor extends Actor with LazyLogging with UserImplicits {

  private val connection = new UserConnection
  protected val verifyUserActor: ActorRef = context.actorOf(Props[VerifyUserActor])

  override def receive: Receive = {
    case req: RegisterRequest => val ref = sender()
      val userSearch = connection.findByUsername(req.username)
      val emailSearch = connection.findByEmail(req.email)
      val mobileSearch = connection.findByMobile(req.mobile)
      (userSearch, emailSearch, mobileSearch) match {
        case (None, None, None) => Future { connection.save(req) } onComplete {
          case Success(_) => connection.findByEmail(req.email) match {
            case Some(res) => ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
            case None => logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case req: TrulayerRegisterRequest => val ref = sender()
      val emailSearch = connection.findByEmail(req.email)
      val mobileSearch = connection.findByMobile(req.mobile)
      (emailSearch, mobileSearch) match {
        case (None, None) => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => connection.findByEmail(req.email) match {
            case Some(res) => ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
            case None => logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case request: LoginRequest => val ref = sender()
      Future {
        connection.findByUsername(request.email)
      } onComplete {
        case Success(result) => result match {
          case None => ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
          case Some(user) => //TODO Check user has verified
            if (user.password.equals(request.password)) {
              ref ! LoginResponse(success = true, id = Some(user.id.get.toString))
            }
            else { ref ! LoginResponse(success = false, Some("INCORRECT_PASSWORD")) }
        }
        case Failure(t) => logger.error("Unable to find user due to", t)
          ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
      }
    case request: PasswordUserLookupRequest => val senderRef = sender()
      Future {
        connection.findByUsername(request.passwordRequest.username)
      } onComplete {
        case Success(user) => senderRef ! PasswordUserLookupResponse(request.actorRef, user)
        case Failure(t) => logger.error(s"Could not find the user due to an error", t)
          senderRef ! PasswordUserLookupResponse(request.actorRef, None)
      }
    case other => logger.error(s"Received unknown message, please check: $other")
  }
}
