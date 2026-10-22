package traits

import java.time.LocalDate

trait Expirable:
  def expiryDate: LocalDate
  def daysUntilExpiry(currentDate: LocalDate = LocalDate.now()): Long =
    java.time.temporal.ChronoUnit.DAYS.between(currentDate, expiryDate)
  def isExpired(currentDate: LocalDate = LocalDate.now()): Boolean =
    expiryDate.isBefore(currentDate)
