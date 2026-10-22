package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.util.Try
import traits.Expirable
import traits.Identifiable

// ai-assisted: #1
// why: AI assistance was used to structure abstract classes and subclasses for perishable and non-perishable inventory items.
abstract class InventoryItem(
    val itemId: String,
    val itemName: String,
    val category: String,
    val quantity: Int,
    val minimumStockLevel: Int,
    val donatedOn: LocalDate
) extends Identifiable:
  override final def id: String = itemId

  def priorityScore: Int

  def withQuantity(newQuantity: Int): InventoryItem

  final def isLowStock: Boolean = quantity <= minimumStockLevel

type PantryItem = InventoryItem

class PerishableItem(
    itemId: String,
    itemName: String,
    category: String,
    quantity: Int,
    minimumStockLevel: Int,
    donatedOn: LocalDate,
    override val expiryDate: LocalDate
) extends InventoryItem(itemId, itemName, category, quantity, minimumStockLevel, donatedOn)
    with Expirable:
  override def priorityScore: Int =
    Try(ChronoUnit.DAYS.between(LocalDate.now(), expiryDate).toInt).toOption match
      case Some(daysUntil) if daysUntil <= 0 => 100
      case Some(daysUntil) if daysUntil <= 3 => 90
      case Some(_) if isLowStock => 75
      case Some(_) => 50
      case None => 0

  override def withQuantity(newQuantity: Int): InventoryItem =
    new PerishableItem(itemId, itemName, category, Math.max(0, newQuantity), minimumStockLevel, donatedOn, expiryDate)

class NonPerishableItem(
    itemId: String,
    itemName: String,
    category: String,
    quantity: Int,
    minimumStockLevel: Int,
    donatedOn: LocalDate,
    val shelfLifeMonths: Int
) extends InventoryItem(itemId, itemName, category, quantity, minimumStockLevel, donatedOn):
  override def priorityScore: Int =
    if isLowStock then 70 else 40

  override def withQuantity(newQuantity: Int): InventoryItem =
    new NonPerishableItem(itemId, itemName, category, Math.max(0, newQuantity), minimumStockLevel, donatedOn, shelfLifeMonths)
