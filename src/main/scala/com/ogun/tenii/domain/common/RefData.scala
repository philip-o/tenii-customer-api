package com.ogun.tenii.domain.common

case class TeniiDate(day: Int, month: Int, year: Int) extends ID

case class Address(address1: String, address2: Option[String], city: String, postCode: String, country: String)

trait ID

case class Passport(passportNumber: Int, issuedLocation: String, expiry: String) extends ID

case class License(licenseNumber: String, expiry: String) extends ID