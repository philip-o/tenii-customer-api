package com.ogun.tenii.actors

import akka.actor.{ Actor, ActorRef, Props }
import com.ogun.tenii.db.{ TellerUserConnection, UserConnection }
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
  private val tellerConnection = new TellerUserConnection
  protected val verifyUserActor: ActorRef = context.actorOf(Props[VerifyUserActor])

  override def receive: Receive = {
    case req: RegisterRequest =>
      val ref = sender()
      val userSearch = connection.findByUsername(req.username)
      val emailSearch = connection.findByEmail(req.email)
      val mobileSearch = connection.findByMobile(req.mobile)
      (userSearch, emailSearch, mobileSearch) match {
        case (None, None, None) => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => connection.findByEmail(req.email) match {
            case Some(res) =>
              ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
            case None =>
              logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    //Check ip hasn't been blocked or given a timeout, if blocked return unable to submit request you have been blocked
    //Check password is valid, if not return invalid password failure
    case req: TellerRegisterRequest =>
      val ref = sender()
      val emailSearch = tellerConnection.findByEmail(req.email)
      val mobileSearch = tellerConnection.findByMobile(req.mobile)
      (emailSearch, mobileSearch) match {
        case (None, None) => Future {
          tellerConnection.save(req)
        } onComplete {
          case Success(_) => tellerConnection.findByEmail(req.email) match {
            case Some(res) =>
              ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
            case None =>
              logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case req: TrulayerRegisterRequest =>
      val ref = sender()
      val emailSearch = connection.findByEmail(req.email)
      val mobileSearch = connection.findByMobile(req.mobile)
      (emailSearch, mobileSearch) match {
        case (None, None) => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => connection.findByEmail(req.email) match {
            case Some(res) =>
              ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
            case None =>
              logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case request: LoginRequest =>
      val ref = sender()
      Future {
        connection.findByUsername(request.username)
      } onComplete {
        case Success(result) => result match {
          case None => ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
          case Some(user) => //TODO Check user has verified
            if (user.password.equals(request.password)) { ref ! LoginResponse(success = true) }
            else { ref ! LoginResponse(success = false, Some("INCORRECT_PASSWORD")) }
        }
        case Failure(t) =>
          logger.error("Unable to find user due to", t)
          ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
      }
    case request: TellerLoginRequest =>
      val ref = sender()
      Future {
        tellerConnection.findByEmail(request.email)
      } onComplete {
        case Success(result) => result match {
          case None => ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
          case Some(user) => //TODO Check user has verified
            if (user.password.equals(request.password)) { ref ! result.get.tellerId.get }
            else { ref ! LoginResponse(success = false, Some("INCORRECT_PASSWORD")) }
        }
        case Failure(t) =>
          logger.error("Unable to find user due to", t)
          ref ! LoginResponse(success = false, Some("USER_NOT_FOUND"))
      }
    case request: PasswordUserLookupRequest =>
      val senderRef = sender()
      Future {
        connection.findByUsername(request.passwordRequest.username)
      } onComplete {
        case Success(user) => senderRef ! PasswordUserLookupResponse(request.actorRef, user)
        case Failure(t) =>
          logger.error(s"Could not find the user due to an error", t)
          senderRef ! PasswordUserLookupResponse(request.actorRef, None)
      }
    case request: TellerAPIPermissionsResponse => Future {
      tellerConnection.findByNoTellerId() match {
        case Some(user) => tellerConnection.save(user.copy(tellerId = Some(request.token)))
        case None => logger.error(s"No user found, investigate and assign user for ${request.token}")
      }
    }
    case other => logger.error(s"Received unknown message, please check: $other")
  }
}
