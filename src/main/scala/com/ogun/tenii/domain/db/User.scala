package com.ogun.tenii.domain.db

import com.ogun.tenii.domain.common.{ Address, ID, RoarType, TeniiDate }
import org.bson.types.ObjectId

case class User(id: Option[ObjectId] = None, title: String = "Mr", forename: String, middle: Option[String] = None, surname: String, address: Address,
  dob: String, username: String, password: String, mobile: String, identification: ID, ipAddress: String, email: String, roarType: RoarType)

case class TellerUser(id: Option[ObjectId] = None, tellerId: Option[String] = None, title: String = "Mr", forename: String, surname: String,
                dob: String, password: String, mobile: String, ipAddress: String, email: String, roarType: RoarType)