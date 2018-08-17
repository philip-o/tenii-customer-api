package com.ogun.tenii

import scala.math.BigDecimal.RoundingMode

object TeniiMortgageApp {

  var mortgageBalance: Double = 100000
  val interestRate = 0.0399
  val monthlyPayment = 528
  var maxYearlyPayment: Double = mortgageBalance * 0.1

  def process(args: Array[String]): Unit = {
    displayBalance()
    displayMaxYearlyBalance()
    removeMonthlyPayment()
    displayBalance()
    displayMaxYearlyBalance()
    addInterestToBalance()
    displayBalance()
    displayMaxYearlyBalance()
    removeMonthlyPayment()
    displayBalance()
    displayMaxYearlyBalance()
    removeOverPayment(10)
    displayBalance()
    displayMaxYearlyBalance()
    addInterestToBalance()
    displayBalance()
    displayMaxYearlyBalance()
  }

  def removeMonthlyPayment(): Unit = {
    mortgageBalance -= monthlyPayment
    maxYearlyPayment -= monthlyPayment
    println("Removed monthly payment")
  }

  def updateMaxYearlyPayment(): Unit = maxYearlyPayment *= 0.1

  def removeOverPayment(extra: Double): Unit = {
    mortgageBalance -= extra
    maxYearlyPayment -= extra
    println(s"Made overpayment of $extra")
  }

  def addInterestToBalance(): Unit = {
    mortgageBalance = mortgageBalance + (mortgageBalance * BigDecimal(interestRate)./(12).toDouble)
    println("Added interest to balance")
  }

  def displayBalance(): Unit = println(s"Mortgage Balance: ${BigDecimal(mortgageBalance).setScale(2, RoundingMode.UP)}")

  def displayMaxYearlyBalance(): Unit = println(s"Remaining max yearly payable balance: $maxYearlyPayment")
}
