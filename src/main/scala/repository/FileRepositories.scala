package repository

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import model.InventoryItem
import model.NonPerishableItem
import model.PerishableItem
import model.RecipientFamily
import scala.jdk.CollectionConverters.*
import scala.util.Try
import traits.Identifiable
import java.util.concurrent.atomic.AtomicReference

class InMemoryRepository[T <: Identifiable] extends Repository[T]:
  private val store = new AtomicReference(Vector.empty[T])

  override def all: Vector[T] = store.get()

  override def add(record: T): Vector[T] =
    store.updateAndGet(_ :+ record)

  def setAll(records: Vector[T]): Vector[T] =
    store.updateAndGet(_ => records)

// ai-assisted: #3
// why: AI assistance was used to create a file storage class to parse and save CSV files.
class FileDataStore(inventoryFile: Path, familyFile: Path):
  // rationale: Encapsulated to prevent external classes from modifying the fallback paths
  private val fallbackFamilyFile: Path = Path.of("src", "main", "resources", "data", "families.csv")
  private val fallbackInventoryFile: Path = Path.of("src", "main", "resources", "data", "inventory.csv")

  def loadFamilies(): Either[String, Vector[RecipientFamily]] =
    Try {
      val targetFile = if Files.exists(familyFile) then familyFile else fallbackFamilyFile
      if Files.exists(targetFile) then
        Files.readAllLines(targetFile).asScala.toVector.drop(1)
      else Vector.empty[String]
    }.toEither.left.map(exception => s"Could not read family data: ${exception.getMessage}").flatMap { familyLines =>
      familyLines.foldLeft(Right(Vector.empty[RecipientFamily]): Either[String, Vector[RecipientFamily]]) {
        case (accumulatedFamilies, familyLine) =>
          for
            currentFamilies <- accumulatedFamilies
            parsedFamily <- parseFamily(familyLine)
          yield currentFamilies :+ parsedFamily
      }
    }

  def saveFamilies(families: Vector[RecipientFamily]): Either[String, Unit] =
    Try {
      Option(familyFile.getParent).foreach(parentDirectory => Files.createDirectories(parentDirectory))
      val header = "familyId|householdName|householdSize|monthlyIncome|dietaryRequirements|priorityLevel"
      val rows = families.map(encodeFamily)
      Files.writeString(familyFile, (header +: rows).mkString(System.lineSeparator()))
      ()
    }.toEither.left.map(exception => s"Could not save family data: ${exception.getMessage}")

  private def encodeFamily(family: RecipientFamily): String =
    Vector(
      family.familyId,
      cleanField(family.householdName),
      family.householdSize.toString,
      family.monthlyIncome.toString,
      family.dietaryRequirements.mkString(";"),
      family.priorityLevel.toString
    ).mkString("|")

  private def parseFamily(familyLine: String): Either[String, RecipientFamily] =
    familyLine.split("\\|", -1).toVector match
      case Vector(familyId, householdName, sizeText, incomeText, dietaryText, priorityText) =>
        for
          householdSize <- parseInt(sizeText, "household size")
          monthlyIncome <- parseDecimal(incomeText, "monthly income")
          priorityLevel <- parseInt(priorityText, "priority level")
        yield
          val dietary = dietaryText.split(";").iterator.map(_.trim).filter(_.nonEmpty).toSet
          RecipientFamily(familyId, householdName, householdSize, monthlyIncome, dietary, priorityLevel)
      case _ => Left("Family data has an invalid column count.")

  private def parseDecimal(text: String, fieldName: String): Either[String, BigDecimal] =
    Try(BigDecimal(text)).toEither.left.map(_ => s"Invalid $fieldName in saved family data.")

  def loadInventory(): Either[String, Vector[InventoryItem]] =
    Try {
      val targetFile = if Files.exists(inventoryFile) then inventoryFile else fallbackInventoryFile
      if Files.exists(targetFile) then
        Files.readAllLines(targetFile).asScala.toVector.drop(1)
      else Vector.empty[String]
    }.toEither.left.map(exception => s"Could not read inventory data: ${exception.getMessage}").flatMap { inventoryLines =>
      inventoryLines.foldLeft(Right(Vector.empty[InventoryItem]): Either[String, Vector[InventoryItem]]) {
        case (accumulatedItems, inventoryLine) =>
          for
            currentItems <- accumulatedItems
            parsedItem <- parseInventory(inventoryLine)
          yield currentItems :+ parsedItem
      }
    }

  def saveInventory(items: Vector[InventoryItem]): Either[String, Unit] =
    Try {
      Option(inventoryFile.getParent).foreach(parentDirectory => Files.createDirectories(parentDirectory))
      val header = "itemId|itemName|category|quantity|minimumStockLevel|donatedOn|itemType|metadata"
      val rows = items.map(encodeInventory)
      Files.writeString(inventoryFile, (header +: rows).mkString(System.lineSeparator()))
      ()
    }.toEither.left.map(exception => s"Could not save inventory data: ${exception.getMessage}")

  private def encodeInventory(item: InventoryItem): String =
    item match
      case perishable: PerishableItem =>
        Vector(
          perishable.itemId,
          cleanField(perishable.itemName),
          cleanField(perishable.category),
          perishable.quantity.toString,
          perishable.minimumStockLevel.toString,
          perishable.donatedOn.toString,
          "Perishable",
          perishable.expiryDate.toString
        ).mkString("|")
      case nonPerishable: NonPerishableItem =>
        Vector(
          nonPerishable.itemId,
          cleanField(nonPerishable.itemName),
          cleanField(nonPerishable.category),
          nonPerishable.quantity.toString,
          nonPerishable.minimumStockLevel.toString,
          nonPerishable.donatedOn.toString,
          "NonPerishable",
          nonPerishable.shelfLifeMonths.toString
        ).mkString("|")

  private def parseInventory(inventoryLine: String): Either[String, InventoryItem] =
    inventoryLine.split("\\|", -1).toVector match
      case Vector(itemId, itemName, category, quantityText, minimumText, donatedText, itemType, metadata) =>
        for
          quantity <- parseInt(quantityText, "quantity")
          minimumStockLevel <- parseInt(minimumText, "minimum stock level")
          donatedOn <- parseDate(donatedText, "donated date")
          item <- itemType match
            case "Perishable" =>
              parseDate(metadata, "expiry date").map(expiryDate =>
                new PerishableItem(itemId, itemName, category, quantity, minimumStockLevel, donatedOn, expiryDate)
              )
            case "NonPerishable" =>
              parseInt(metadata, "shelf life").map(shelfLifeMonths =>
                new NonPerishableItem(itemId, itemName, category, quantity, minimumStockLevel, donatedOn, shelfLifeMonths)
              )
            case unrecognisedType => Left(s"Unrecognised inventory type: $unrecognisedType")
        yield item
      case _ => Left("Inventory data has an invalid column count.")

  private def parseInt(text: String, fieldName: String): Either[String, Int] =
    Try(text.toInt).toEither.left.map(_ => s"Invalid $fieldName in saved inventory data.")

  private def parseDate(text: String, fieldName: String): Either[String, LocalDate] =
    Try(LocalDate.parse(text)).toEither.left.map(_ => s"Invalid $fieldName in saved inventory data.")

  private def cleanField(text: String): String = text.replace("|", "/").trim
