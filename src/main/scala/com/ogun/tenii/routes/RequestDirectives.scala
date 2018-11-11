package com.ogun.tenii.routes

import akka.http.scaladsl.server.{Directive1, Directives}
import com.ogun.tenii.domain.api.TellerPermissions

trait RequestDirectives extends Directives {

  val accessTokenDirective: Directive1[String] = parameter("token")//.map(LastName(_))
  val permissionsDirective: Directive1[List[TellerPermissions]] = parameter("permissions").map(perm => perm.split(",")
    .map(va => TellerPermissions(va.split(":")(0).toString, va.split(":")(1).toBoolean)).toList)

  val userIdDirective: Directive1[String] = parameter("userId")
}
