package service

import java.time.LocalDate
import model.InventoryItem
import model.NonPerishableItem
import model.PerishableItem
import model.RecipientFamily

// ai-assisted: #8
// why: AI assistance was used to create a sample data loader to pre-populate pantry inventory.
object SampleData:
  private val currentDate: LocalDate = LocalDate.now()

  val inventory: Vector[InventoryItem] = Vector(
    new PerishableItem("ITM-1001", "Fresh milk", "Dairy", 12, 15, currentDate.minusDays(1), currentDate.plusDays(2)),
    new PerishableItem("ITM-1002", "Mixed vegetables", "Produce", 24, 10, currentDate, currentDate.plusDays(5)),
    new NonPerishableItem("ITM-1003", "Rice", "Staples", 40, 20, currentDate.minusDays(4), 24),
    new NonPerishableItem("ITM-1004", "Canned beans", "Protein", 8, 12, currentDate.minusDays(2), 36)
  )

  val families: Vector[RecipientFamily] = Vector(
    RecipientFamily("FAM-2001", "Rahman household", 5, BigDecimal(1800), Set("Halal"), 5),
    RecipientFamily("FAM-2002", "Tan household", 3, BigDecimal(2200), Set("Vegetarian"), 4),
    RecipientFamily("FAM-2003", "Kaur household", 2, BigDecimal(2600), Set.empty, 2)
  )
