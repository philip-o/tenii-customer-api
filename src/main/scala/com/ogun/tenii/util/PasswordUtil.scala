package com.ogun.tenii.util

object PasswordUtil {

  //[0-9]
  val NUMBER_ASCII_CHARACTERS = Set(48, 57)

  //[- A-Za-zÀ-ÿ0-9]
  val ASCII_CHARACTERS: Seq[Int] = (65 to 90) ++ (97 to 122) ++ Set(32, 45) ++ NUMBER_ASCII_CHARACTERS

  def isPasswordValid(password: String): Boolean = {
    if (password.length > 9 && doesPasswordContainsValidCharacters(password) && doesPasswordContainOneNumber(password)) {
      true
    } else
      false
  }

  private def doesPasswordContainsValidCharacters(password: String) = {
    val chars = password map (_.toInt)
    if (chars forall ASCII_CHARACTERS.contains) {
      true
    } else
      false
  }

  private def doesPasswordContainOneNumber(password: String) = {
    val chars = password map (_.toInt)
    if (chars.exists(NUMBER_ASCII_CHARACTERS.contains)) {
      true
    } else
      false
  }
}
