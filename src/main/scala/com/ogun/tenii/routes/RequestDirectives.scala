package com.ogun.tenii.routes

import akka.http.scaladsl.server.{ Directive1, Directives }

trait RequestDirectives extends Directives {

  val accessTokenDirective: Directive1[String] = parameter("token") //.map(LastName(_))

  val userIdDirective: Directive1[String] = parameter("userId")

  val emailDirective: Directive1[String] = parameter("email")

}
