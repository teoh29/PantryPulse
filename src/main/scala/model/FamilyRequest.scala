package model

import traits.Identifiable


case class RecipientFamily(
    familyId: String,
    householdName: String,
    householdSize: Int,
    monthlyIncome: BigDecimal,
    dietaryRequirements: Set[String],
    priorityLevel: Int
) extends Identifiable:
  private val eligibilityLabel: String =
    if monthlyIncome <= BigDecimal(2000) then "High need" else "Standard need"

  override def id: String = familyId

  def isHighNeed: Boolean = priorityLevel >= 4

  def eligibilitySummary: String = eligibilityLabel

type FamilyRequest = RecipientFamily
