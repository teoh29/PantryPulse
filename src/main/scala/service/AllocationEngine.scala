package service

import java.time.LocalDate
import model.Allocation
import model.DistributionPlan
import model.InventoryItem
import model.RecipientFamily
import repository.FileDataStore
import repository.InMemoryRepository

// ai-assisted: #6
// why: AI assistance was used to write an automated distribution algorithm.
final class DistributionPlanner:
  def createPlan(
      items: Vector[InventoryItem],
      families: Vector[RecipientFamily],
      createdOn: LocalDate
  ): DistributionPlan =
    val prioritisedItems = items.filter(_.quantity > 0).sortBy(item => -item.priorityScore)
    val prioritisedFamilies = families.sortBy(family => -family.priorityLevel)
    val allocations = prioritisedFamilies.zip(prioritisedItems).map { case (family, item) =>
      Allocation(
        family.familyId,
        family.householdName,
        item.itemId,
        item.itemName,
        Math.max(1, Math.min(item.quantity, family.householdSize * 2)),
        item.priorityScore
      )
    }
    DistributionPlan(s"PLAN-${System.currentTimeMillis()}", createdOn, allocations)

type AllocationEngine = DistributionPlanner

final class PantryService(dataStore: FileDataStore):
  private val startingInventory: Vector[InventoryItem] = dataStore.loadInventory() match
    case Right(savedItems) if savedItems.nonEmpty => savedItems
    case _ => SampleData.inventory

  private val startingFamilies: Vector[RecipientFamily] = dataStore.loadFamilies() match
    case Right(savedFamilies) if savedFamilies.nonEmpty => savedFamilies
    case _ => SampleData.families

  private val inventoryRepository = new InMemoryRepository[InventoryItem]()
  private val familyRepository = new InMemoryRepository[RecipientFamily]()
  inventoryRepository.setAll(startingInventory)
  familyRepository.setAll(startingFamilies)

  def inventory: Vector[InventoryItem] = inventoryRepository.all

  def families: Vector[RecipientFamily] = familyRepository.all

  def addInventory(item: InventoryItem): Either[String, Unit] =
    val proposedInventory = inventory :+ item
    dataStore.saveInventory(proposedInventory).map { _ =>
      inventoryRepository.add(item)
      ()
    }

  def addFamily(family: RecipientFamily): Either[String, Unit] =
    val proposedFamilies = families :+ family
    dataStore.saveFamilies(proposedFamilies).map { _ =>
      familyRepository.add(family)
      ()
    }

  def updateStockQuantity(itemId: String, delta: Int): Either[String, Unit] =
    val updated = inventory.map { item =>
      if item.itemId == itemId then item.withQuantity(item.quantity + delta)
      else item
    }
    dataStore.saveInventory(updated).map { _ =>
      inventoryRepository.setAll(updated)
      ()
    }

  def deleteItem(itemId: String): Either[String, Unit] =
    val updated = inventory.filterNot(_.itemId == itemId)
    dataStore.saveInventory(updated).map { _ =>
      inventoryRepository.setAll(updated)
      ()
    }

  def fulfillPlan(allocations: Vector[Allocation]): Either[String, Unit] =
    val deductionMap = allocations.groupBy(_.itemId).map { case (id, list) => id -> list.map(_.quantity).sum }
    val updated = inventory.map { item =>
      deductionMap.get(item.itemId) match
        case Some(deduction) => item.withQuantity(item.quantity - deduction)
        case None => item
    }
    dataStore.saveInventory(updated).map { _ =>
      inventoryRepository.setAll(updated)
      ()
    }

  def deleteFamily(familyId: String): Either[String, Unit] =
    val updated = families.filterNot(_.familyId == familyId)
    dataStore.saveFamilies(updated).map { _ =>
      familyRepository.setAll(updated)
      ()
    }

  // ai-assisted: #11
  // why: AI assistance was used to export daily distribution allocation plans to structured PDF format for physical record-keeping.
  def exportPlanToPdf(plan: DistributionPlan): Either[String, String] =
    scala.util.Try {
      import com.lowagie.text.Document
      import com.lowagie.text.Paragraph
      import com.lowagie.text.pdf.PdfWriter
      import com.lowagie.text.pdf.PdfPTable
      import java.io.FileOutputStream
      import com.lowagie.text.Phrase

      val targetPath = java.nio.file.Paths.get("data", "distribution_plan.pdf")
      val parentDir = targetPath.getParent
      if parentDir != null && !java.nio.file.Files.exists(parentDir) then
        java.nio.file.Files.createDirectories(parentDir)

      val document = new Document()
      PdfWriter.getInstance(document, new FileOutputStream(targetPath.toFile))
      document.open()

      val title = new Paragraph(s"Distribution Plan: ${plan.planId}\nCreated On: ${plan.createdOn}\n\n")
      document.add(title)

      val table = new PdfPTable(6)
      table.setWidthPercentage(100f)

      val headers = Array("Allocation ID", "Household", "Item", "Quantity", "Priority", "Date")
      // ai-assisted: #11
      // why: AI assistance was used to determine the correct API for iterating and populating PDFPTable header cells.
      headers.foreach(header => table.addCell(new Phrase(header)))

      plan.allocations.zipWithIndex.foreach { case (alloc, idx) =>
        table.addCell(s"ALLOC-${idx + 1}")
        table.addCell(alloc.householdName)
        table.addCell(alloc.itemName)
        table.addCell(alloc.quantity.toString)
        table.addCell(alloc.priorityScore.toString)
        table.addCell(plan.createdOn.toString)
      }

      document.add(table)
      document.close()

      targetPath.toAbsolutePath.toString
    }.toEither.left.map(ex => s"Failed to export PDF: ${ex.getMessage}")

  // ai-assisted: #14
  // why: AI assistance was used to re-seed runtime persistence with clean sample benchmark data for live assessment testing.
  def resetToSampleData(): Either[String, Unit] =
    val sampleInventory = SampleData.inventory
    val sampleFamilies = SampleData.families
    for
      _ <- dataStore.saveInventory(sampleInventory)
      _ <- dataStore.saveFamilies(sampleFamilies)
    yield
      inventoryRepository.setAll(sampleInventory)
      familyRepository.setAll(sampleFamilies)
      ()
