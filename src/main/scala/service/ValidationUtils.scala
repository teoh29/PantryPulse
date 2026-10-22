package service

import scala.util.Try

// ai-assisted: #5
// why: AI assistance was used to validate numeric text fields safely.
object InputValidator:
  def requiredText(fieldName: String, value: String): Either[String, String] =
    val trimmedValue = value.trim
    if trimmedValue.isEmpty then Left(s"$fieldName is required.")
    else if trimmedValue.contains("|") then Left(s"$fieldName cannot contain the | character.")
    else Right(trimmedValue)

  def positiveInt(fieldName: String, value: String): Either[String, Int] =
    Try(value.trim.toInt).toEither.left.map(_ => s"$fieldName must be a whole number.").flatMap { parsedValue =>
      if parsedValue > 0 then Right(parsedValue)
      else Left(s"$fieldName must be greater than zero.")
    }

  def nonNegativeDecimal(fieldName: String, value: String): Either[String, BigDecimal] =
    Try(BigDecimal(value.trim)).toEither.left.map(_ => s"$fieldName must be a valid amount.").flatMap { parsedValue =>
      if parsedValue >= BigDecimal(0) then Right(parsedValue)
      else Left(s"$fieldName cannot be negative.")
    }

type ValidationUtils = InputValidator.type
