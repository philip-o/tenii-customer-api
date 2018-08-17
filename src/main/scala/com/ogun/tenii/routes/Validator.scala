package com.ogun.tenii.routes

import cats.data.Validated.{ invalidNel, valid }
import cats.data.{ Validated, NonEmptyList => NEL }
import cats.implicits._

import scala.collection.immutable
import scala.util.Try

object Validator {

  //  val FORENAME_MIN_LENGTH = 1
  //  val STRING_MIN_LENGTH = 2
  //  val NAME_MAX_LENGTH = 32
  //  val ADDRESS_MAX_LENGTH = 35
  //  val POST_CODE_MAX_LENGTH = 10
  //  val EMAIL_MAX_LENGTH = 100
  //  //[- A-Za-zÀ-ÿ]
  //  val ASCII_CHARACTERS = (65 to 90) ++ (97 to 122) ++ (192 to 255) ++ Set(32, 45)
  //
  //  def isValidArrivalDate(arrivalDate: ArrivalDate): Validated[NEL[ErrorResponse], String] = {
  //    Try(
  //      DateUtil.parseDate(DateUtil.dateFormatter1, arrivalDate.arrivalDate)
  //    ).transform(
  //      _ => Try(valid[NEL[ErrorResponse], String]("Date is valid")),
  //      _ => Try(invalidNel[ErrorResponse, String](ErrorResponse(
  //        RequestValidationError.errorCode,
  //        Option(s"Date: '${arrivalDate.arrivalDate}' should comply with format ${DateUtil.dateFormat1} and be a valid date")
  //      )))
  //    ).get
  //  }
  //
  //  def isValidMealBasis(mealBasis: Option[MealBasis]): Validated[NEL[ErrorResponse], String] = {
  //    if (mealBasis.isEmpty || mealBasis.get.mealBasis < 1000) {
  //      valid[NEL[ErrorResponse], String]("MealBasis is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"MealBasis: '${mealBasis.get.mealBasis}' should take one of the allowed values")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidDuration(duration: Duration): Validated[NEL[ErrorResponse], String] = {
  //    if (duration.duration > 0 && duration.duration < 100) {
  //      valid[NEL[ErrorResponse], String]("Duration is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Duration: '${duration.duration}' is invalid")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidPartyComposition(rooms: immutable.Seq[prebook.RoomBooking]): Validated[NEL[ErrorResponse], String] = {
  //
  //    val isAdultValid = isValidNumberOfAdults(rooms.map(_.ages.length).sum)
  //
  //    if (isAdultValid) {
  //      valid[NEL[ErrorResponse], String]("Party composition is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Party composition is invalid: $rooms")
  //        )
  //      )
  //    }
  //  }
  //
  //  private def isValidNumberOfAdults(numberOfAdults: Int) = numberOfAdults >= 1 && numberOfAdults <= 9
  //
  //  /**
  //    * This validation es equivalent to the one made on BPA using the regular expression [- A-Za-zÀ-ÿ]
  //    */
  //  def isValidString(form: String, validCharacters: Seq[Int] = ASCII_CHARACTERS): Validated[NEL[ErrorResponse], String] = {
  //    val formAsAsciiValues = form.map(_.toInt)
  //    if (formAsAsciiValues.forall(validCharacters.contains(_))) {
  //      valid[NEL[ErrorResponse], String]("String is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"String contains characters outside of the accepted set of values")
  //        )
  //      )
  //    }
  //
  //  }
  //
  //  def isNotEmptyString(form: String): Validated[NEL[ErrorResponse], String] = {
  //
  //    if (!form.isEmpty) {
  //      valid[NEL[ErrorResponse], String]("String is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"String is empty")
  //        )
  //      )
  //    }
  //
  //  }
  //
  //  def isValidNameCharacterLength(form: String, minimumLength: Int = STRING_MIN_LENGTH): Validated[NEL[ErrorResponse], String] = isValidCharacterLength(form, minimumLength)
  //
  //  def isValidAddressCharacterLength(form: String, length: Int = ADDRESS_MAX_LENGTH): Validated[NEL[ErrorResponse], String] =
  //    isValidCharacterLength(form, STRING_MIN_LENGTH, length)
  //
  //  def isValidCharacterLength(form: String, minimumLength: Int, maxLength: Int = NAME_MAX_LENGTH): Validated[NEL[ErrorResponse], String] = {
  //
  //    if (form.length >= minimumLength && form.length <= maxLength) {
  //      valid[NEL[ErrorResponse], String]("String is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"String is greater than $maxLength characters long")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidRoomGuests(request: BookBody): Validated[NEL[ErrorResponse], String] = {
  //    val guests = request.rooms flatMap {
  //      _.guests
  //    }
  //    if (guests.forall(guest => isValidForename(guest.firstName).isValid && isValidSurname(guest.lastName).isValid)) {
  //      valid[NEL[ErrorResponse], String]("Guest names are valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"One or more invalid guests names, please check")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidAddressBasic(form: String): Validated[NEL[ErrorResponse], String] = {
  //    (isNotEmptyString(form) |@| isValidString(form) |@| isValidAddressCharacterLength(form)).map(_ + _ + _)
  //  }
  //
  //  def isValidAddressLine(form: String): Validated[NEL[ErrorResponse], String] = {
  //    (isNotEmptyString(form) |@| isValidString(form, ASCII_CHARACTERS ++ (48 to 57) ++ (44 to 46) ++ Set(39)) |@| isValidAddressCharacterLength(form)).map(_ + _ + _)
  //  }
  //
  //  def isValidEmail(email: String): Validated[NEL[ErrorResponse], String] = {
  //    if (!email.isEmpty && """[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])""".r.unapplySeq(email).isDefined && email.length <= EMAIL_MAX_LENGTH) {
  //      valid[NEL[ErrorResponse], String]("Email is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Email address entered is invalid, please check")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidPhoneNumber(number: String): Validated[NEL[ErrorResponse], String] = {
  //    if (!number.isEmpty && """^[\d ()+-]{6,13}$""".r.unapplySeq(number).isDefined) {
  //      valid[NEL[ErrorResponse], String]("Phone number is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Phone number entered is invalid, please check")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidPostCode(postCode: String): Validated[NEL[ErrorResponse], String] = {
  //    if (!postCode.isEmpty && postCode.matches("^(.{0,10}|\\d{5}(-\\d{5})?)$") && postCode.length <= POST_CODE_MAX_LENGTH) {
  //      valid[NEL[ErrorResponse], String]("Post code is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Post code entered is invalid, please check")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidBillingAddress(body: BookBody): Validated[NEL[ErrorResponse], String] = {
  //    val add = body.billingDetails.address
  //    if (isValidAddress(add).isValid) {
  //      valid[NEL[ErrorResponse], String]("Billing address is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"One or more parts of the billing address are invalid, please check")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidAddress(add: Address): Validated[NEL[ErrorResponse], String] = {
  //    if (isValidAddressLine(add.address1).isValid
  //      && (add.address2.isEmpty || isValidAddressLine(add.address2.get).isValid)
  //      && isValidAddressBasic(add.townCity).isValid && (add.county.isEmpty || isValidAddressBasic(add.county.get).isValid)
  //      && isValidPostCode(add.postCode).isValid && isValidAddressBasic(add.country).isValid) {
  //      valid[NEL[ErrorResponse], String]("Address is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"Invalid address")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isDefinedString(field: String, value: Option[String]): Validated[NEL[ErrorResponse], String] = {
  //
  //    if (value.isDefined) {
  //      valid[NEL[ErrorResponse], String](s"$field is valid")
  //    } else {
  //      invalidNel[ErrorResponse, String](
  //        ErrorResponse(
  //          RequestValidationError.errorCode,
  //          Option(s"$field is empty")
  //        )
  //      )
  //    }
  //  }
  //
  //  def isValidForename(form: String): Validated[NEL[ErrorResponse], String] = isValidName(form, FORENAME_MIN_LENGTH)
  //
  //  def isValidSurname(form: String): Validated[NEL[ErrorResponse], String] = isValidName(form, STRING_MIN_LENGTH)
  //
  //  //Included ascii character 39 for apostrophe
  //  def isValidName(form: String, minimumLength: Int = STRING_MIN_LENGTH): Validated[NEL[ErrorResponse], String] = {
  //    (isNotEmptyString(form) |@| isValidString(form, ASCII_CHARACTERS ++ Set(39)) |@| isValidNameCharacterLength(form, minimumLength)).map(_ + _ + _)
  //  }

}

