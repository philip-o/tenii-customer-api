package com.ogun.tenii.domain.password

import akka.actor.ActorRef
import com.ogun.tenii.domain.api.PasswordResetRequest
import com.ogun.tenii.domain.db.User

case class PasswordUserLookupRequest(actorRef: ActorRef, passwordRequest: PasswordResetRequest)

case class PasswordUserLookupResponse(actorRef: ActorRef, user: Option[User])
