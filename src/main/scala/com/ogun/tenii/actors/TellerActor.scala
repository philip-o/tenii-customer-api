package com.ogun.tenii.actors

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.ogun.tenii.domain.api._
import com.ogun.tenii.domain.teller.{ TellerAccountsRequest, TellerAccountsResponse, TellerResponse }
import com.ogun.tenii.external.HttpTransfers
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{ Failure, Properties, Success }

class TellerActor extends Actor with TellerEndpoints with LazyLogging with TeniiEndpoints {

  implicit val system: ActorSystem = context.system
  val http = new HttpTransfers()
  val tellerAppId: String = Properties.envOrElse("TELLER_APP_ID", "blabla")
  val userActor: ActorRef = system.actorOf(Props(classOf[UserActor]))
  implicit val timeout: FiniteDuration = 10.seconds

  override def receive: Receive = {
    case req: TellerAPIPermissionsResponse =>
      val senderRef = sender()
      if (validatePermissions(providedPermissions = req.permissions)) {
        userActor ! req
        senderRef ! TellerPermissionsResponse(Some(req.token))
        //TODO Load limit from register request and then update
        http.endpoint[TellerTeniiPotCreateRequest, TellerTeniiPotCreateResponse](
          s"$paymentsApiHost$createPot",
          TellerTeniiPotCreateRequest(req.token, 100)
        ) onComplete {
            case Success(resp) => logger.info(s"Created a Tenii pot for user $resp")
            case Failure(t) => logger.error(s"Failed to create Tenii pot, please check and fix: $req", t)
          }
      } else {
        senderRef ! TellerPermissionsResponse(None, Some("Required permissions not given"))
      }
    case request: LoginRequest =>
      val senderRef = sender()
      http.endpointGet[List[TellerResponse]](s"$productsApiHost$bankAccounts", ("Authorization", s"Bearer ${request.email}")).onComplete {
        case Success(resp) => senderRef ! resp
        case Failure(t) => sender() ! t
      }
    case req: TellerRegisterRequest =>
      val senderRef = sender()
      implicit val timeout: Timeout = Timeout(10.seconds)
      (userActor ? req) onComplete {
        case Success(_) => senderRef ! s"$tellerHost$auth$appId$tellerAppId&permissions=$permissions"
        case Failure(t) => logger.error(s"Error thrown when attempting to register user", t)
      }
    case request: TellerLoginRequest =>
      val senderRef = sender()
      implicit val timeout: Timeout = Timeout(10.seconds)
      (userActor ? request) onComplete {
        case Success(resp) =>
          resp match {
            case res: LoginResponse => senderRef ! TellerLoginResponse("", res.errorCode)
            case id: String =>
              implicit val timeout2: FiniteDuration = 10.seconds
              http.endpoint[TellerAccountsRequest, List[TellerResponse]](s"$productsApiHost$bankAccounts", TellerAccountsRequest(id)).onComplete {
                case Success(resp) => senderRef ! TellerAccountsResponse(id, resp)
                case Failure(t) => sender() ! t
              }
          }
        case Failure(t) =>
          logger.error(s"Error thrown when attempting to find user user", t)
          senderRef ! TellerLoginResponse("", Some(s"Error thrown when attempting to find user user"))
      }
    case other =>
      logger.info(s"Unknown message received: $other")
  }

}

trait TellerEndpoints {

  val tellerHost = "https://teller.io/"
  val auth = "auth/authorize?"
  val appId = "application_id="
  val permissions = "balance:true,full_account_number:true,transaction_history:true"
  val requiredPermissions = permissions.split(",").map(_.split(":"))
    .map(perm => TellerPermissions(perm(0), calculateBool(perm(1)))).toList

  def calculateBool(bool: String): Boolean = {
    bool match {
      case "true" => true
      case _ => false
    }
  }

  implicit def onSuccessDecodingError[TellerResponse](decodingError: io.circe.Error): TellerResponse = throw new Exception(s"Error decoding trains upstream response: $decodingError")
  implicit def onErrorDecodingError[TellerResponse](decodingError: String): TellerResponse = throw new Exception(s"Error decoding upstream error response: $decodingError")

  def validatePermissions(requestedPermissions: List[TellerPermissions] = requiredPermissions, providedPermissions: List[TellerPermissions], matches: Boolean = true): Boolean = {
    requestedPermissions match {
      case Nil => matches
      case head :: tail => if (!matches) {
        matches
      } else {
        validatePermissions(tail, providedPermissions, providedPermissions.contains(head))
      }
    }
  }
}

trait TeniiEndpoints {

  val paymentsApiHost = "https://tenii-payments-api.herokuapp.com/"
  val productsApiHost = "https://tenii-products-api.herokuapp.com/"
  val createPot = "teller/createPot/"
  val bankAccounts = "teller/bankAccounts"
}

