package com.ogun.tenii.domain.common

case class RoarType(roar: String, limit: Int)

object Roar extends Enumeration {
  type Roar = Value
  val BALANCED = Value("Balanced")
  val HUNT = Value("Hunt")
  val STRIPES = Value("Stripes")
}