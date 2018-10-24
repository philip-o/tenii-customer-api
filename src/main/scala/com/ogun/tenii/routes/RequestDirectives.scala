package com.ogun.tenii.routes

import akka.http.scaladsl.server.{Directive1, Directives}
import com.ogun.tenii.domain.api.TellerPermissions

trait RequestDirectives extends Directives {

  val accessTokenDirective: Directive1[String] = parameter("token")//.map(LastName(_))
  val permissionsDirective: Directive1[List[TellerPermissions]] = parameter("permissions").map(perm => perm.split(",")
    .map(va => TellerPermissions(va.split(":")(0).toString, va.split(":")(1).toBoolean)).toList)

  val userIdDirective: Directive1[String] = parameter("userId")


  //val prebookingTokenDirective = parameter("preBookingToken".?).map(_.map(PreBookingToken))

  // def validateRequest(req: BookRequest): Directive0 = {
  //
  //    val result = (isValidArrivalDate(req.body.arrivalDate) |@| isValidDuration(req.body.duration)
  //      |@| isValidForename(req.body.leadGuest.firstName) |@| isValidSurname(req.body.leadGuest.lastName)
  //      |@| isValidRoomGuests(req.body) |@| isValidEmail(req.body.leadGuest.email)
  //      |@| isValidPhoneNumber(req.body.leadGuest.phone) |@| isValidBillingAddress(req.body)
  //      |@| isValidTrains(req.body.trains) |@| isValidMembershipNumber(req.body.leadGuest.membershipNumber))
  //      .map(_ + _ + _ + _ + _ + _ + _ + _ + _ + _)
  //
  //    result match {
  //      case Valid(_) => pass
  //      case Invalid(_) => reject(RequestValidationRejection(ErrorResponseList(toListOfErrorResponse(result))))
  //    }
  //
  //  }

}
