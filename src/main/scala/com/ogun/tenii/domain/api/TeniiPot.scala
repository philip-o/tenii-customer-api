package com.ogun.tenii.domain.api

case class TeniiPotCreateRequest(teniiId: String, limit: Int)

case class TeniiPotCreateResponse(cause: Option[String] = None)
