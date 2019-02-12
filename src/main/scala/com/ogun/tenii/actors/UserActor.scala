package com.ogun.tenii.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.ogun.tenii.db.UserConnection
import com.ogun.tenii.domain.api
import com.ogun.tenii.domain.api._
import com.ogun.tenii.domain.impicits.UserImplicits
import com.ogun.tenii.domain.password.{PasswordUserLookupRequest, PasswordUserLookupResponse}
import com.ogun.tenii.domain.verifyuser.VerifyEmailPersistRequest
import com.ogun.tenii.external.{HttpTransfers, TeniiEndpoints}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class UserActor extends Actor with LazyLogging with UserImplicits with TeniiEndpoints {

  private val connection = new UserConnection
  protected val verifyUserActor: ActorRef = context.actorOf(Props[VerifyUserActor])
  implicit val system: ActorSystem = context.system
  val http = new HttpTransfers()

  override def receive: Receive = {
      //TODO Remove.  Duplicated below with provider
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
              implicit val timeout: FiniteDuration = 30.seconds
              http.endpoint[TrulayerAddUserRequest, String](s"$trulayerApiHost$addTeniiId",
                TrulayerAddUserRequest(res.id.get.toString)) onComplete {
                case Success(_) => logger.info(s"Added new tenii user to cache")
                case Failure(t) => logger.error(s"Error thrown while trying to create entry for id", t)
              }
              http.endpoint[TeniiPotCreateRequest, TeniiPotCreateResponse](
                s"$paymentsApiHost$createPot",
                TeniiPotCreateRequest(res.id.get.toString, req.roarType.limit)
              ) onComplete {
                case Success(_) => logger.info(s"Created a Tenii pot for user ${res.id.get.toString}")
                case Failure(t) => logger.error(s"Failed to create Tenii pot, please check and fix: $req", t)
              }
            case None => logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case req: TrulayerRegisterRequestV2 => val ref = sender()
      val emailSearch = connection.findByEmail(req.email)
      val mobileSearch = connection.findByMobile(req.mobile)
      (emailSearch, mobileSearch) match {
        case (None, None) => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => connection.findByEmail(req.email) match {
            case Some(res) => ref ! RegisterResponse(res.email, res.mobile)
              verifyUserActor ! VerifyEmailPersistRequest(res.id.get, res.email)
              implicit val timeout: FiniteDuration = 30.seconds
              http.endpoint[TrulayerAddUserRequest, String](s"$trulayerApiHost$addTeniiId",
                TrulayerAddUserRequest(res.id.get.toString)) onComplete {
                case Success(_) => logger.info(s"Added new tenii user to cache")
                case Failure(t) => logger.error(s"Error thrown while trying to create entry for id", t)
              }
              http.endpoint[TeniiPotCreateRequest, TeniiPotCreateResponse](
                s"$paymentsApiHost$createPot",
                TeniiPotCreateRequest(res.id.get.toString, req.roarType.limit)
              ) onComplete {
                case Success(_) => logger.info(s"Created a Tenii pot for user ${res.id.get.toString}")
                case Failure(t) => logger.error(s"Failed to create Tenii pot, please check and fix: $req", t)
              }
            case None => logger.error(s"Unable to find user by email: ${req.email}")
              ref ! RegisterResponse(req.email, req.mobile, success = false, Some(s"Unable to find user by email: ${req.email}"))
          }
          case Failure(t) => ref ! new Exception("Failed to save", t)
        }
        case _ => sender() ! RegisterResponse(req.email, req.mobile, success = false, Some("Credentials already used"))
      }
    case request: LoginRequest => val ref = sender()
      Future {
        connection.findByEmail(request.email)
      } onComplete {
        case Success(result) => result match {
          case None => ref ! ErrorResponse("USER_NOT_FOUND")
          case Some(user) => //TODO Check user has verified
            if (user.password.equals(request.password)) {
              implicit val timeout: FiniteDuration = 30.seconds
              http.endpoint[TrulayerAddUserRequest, TrulayerAccountsResponse](s"$trulayerApiHost$login",
                TrulayerAddUserRequest(user.id.get.toString)) onComplete {
                case Success(resp) => ref ! LoginResponse(accounts = resp.accounts, teniiId = Some(user.id.get.toString))
                case Failure(t) => logger.error(s"Failed to load accounts, please check and fix: ${request.email}", t)
                  ref ! ErrorResponse("ACCOUNT_LOAD_ERROR", Some("Failed to create load accounts"))
              }
            }
            else { ref ! ErrorResponse("INCORRECT_PASSWORD") }
        }
        case Failure(t) => logger.error("Unable to find user due to", t)
          ref ! ErrorResponse("USER_NOT_FOUND")
      }
    case other => logger.error(s"Received unknown message, please check: $other")
  }
}
