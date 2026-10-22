package model

import java.time.LocalDate
import traits.Identifiable

case class Allocation(
    familyId: String,
    householdName: String,
    itemId: String,
    itemName: String,
    quantity: Int,
    priorityScore: Int
)

case class DistributionPlan(
    planId: String,
    createdOn: LocalDate,
    allocations: Vector[Allocation]
) extends Identifiable:
  override def id: String = planId

  def totalAllocatedUnits: Int = allocations.map(_.quantity).sum
