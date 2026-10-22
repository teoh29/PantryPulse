package ui

import java.time.LocalDate
import model.InventoryItem
import model.NonPerishableItem
import model.PerishableItem
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Button
import scalafx.scene.control.ButtonType
import scalafx.scene.control.ComboBox
import scalafx.scene.control.DatePicker
import scalafx.scene.control.Label
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.TableColumn
import scalafx.scene.control.TableView
import scalafx.scene.control.TextField
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.Priority
import scalafx.scene.layout.VBox
import service.InputValidator
import service.PantryService

// ai-assisted: #7
// why: AI assistance was used to attach button event handlers in ScalaFX TableView forms.
final class InventoryView(pantryService: PantryService):
  private val itemNameField = new TextField:
    promptText = "e.g. Rice"
    style = UiTheme.textFieldStyle
  private val categoryField = new TextField:
    promptText = "e.g. Staples"
    style = UiTheme.textFieldStyle
  private val quantityField = new TextField:
    promptText = "e.g. 20"
    style = UiTheme.textFieldStyle
  private val minimumStockField = new TextField:
    promptText = "e.g. 10"
    style = UiTheme.textFieldStyle

  quantityField.text.onChange { (_, _, newValue) =>
    if newValue != null && !newValue.matches("\\d*") then
      quantityField.text = newValue.replaceAll("[^0-9]", "")
  }
  minimumStockField.text.onChange { (_, _, newValue) =>
    if newValue != null && !newValue.matches("\\d*") then
      minimumStockField.text = newValue.replaceAll("[^0-9]", "")
  }

  private val itemTypeBox = new ComboBox[String](ObservableBuffer("Perishable", "Non-perishable")):
    value = "Perishable"
    maxWidth = Double.MaxValue
  private val expiryDatePicker = new DatePicker:
    promptText = "Required for perishables"
    maxWidth = Double.MaxValue
  private val statusLabel = new Label():
    wrapText = true
  private val tableRows = ObservableBuffer.empty[InventoryItem]
  private val inventoryTable = new TableView[InventoryItem](tableRows):
    prefHeight = 380
    minHeight = 250
    columnResizePolicy = TableView.ConstrainedResizePolicy
    style = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;"
    placeholder = new Label("No inventory items found."):
      style = "-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13px;"
    columns ++= Seq(
      inventoryColumn("Item", _.itemName, 180, alignCenter = true),
      inventoryColumn("Category", _.category, 110, alignCenter = true),
      inventoryColumn("Units", item => item.quantity.toString, 70, alignCenter = true),
      inventoryColumn("Minimum", item => item.minimumStockLevel.toString, 80, alignCenter = true),
      inventoryColumn("Type", itemTypeLabel, 110, alignCenter = true),
      inventoryColumn("Priority", item => item.priorityScore.toString, 80, alignCenter = true)
    )

  private val saveButton = UiTheme.primaryButton("Save inventory item")
  saveButton.defaultButton = true
  saveButton.maxWidth = Double.MaxValue
  saveButton.onAction = _ => { submitItem() }
  itemTypeBox.onAction = _ => { expiryDatePicker.disable = itemTypeBox.value.value != "Perishable" }

  private val mainContent = new HBox(24):
    padding = Insets(0, 0, 0, 0)
    children = Seq(donationPanelCard, tablePanel)

  HBox.setHgrow(tablePanel, Priority.Always)

  private val pageLayout = new VBox(24):
    padding = Insets(8, 32, 48, 24)
    children = Seq(mainContent)

  private val mainScroll = new ScrollPane:
    content = pageLayout
    fitToWidth = true
    style = "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"

  val root: BorderPane = new BorderPane:
    center = mainScroll
    style = UiTheme.appBackground

  refresh()

  def refresh(): Unit = tableRows.setAll(pantryService.inventory: _*)

  // ─── Donation form card ────────────────────────────────────────────
  private lazy val donationForm: VBox = new VBox(14):
    children = Seq(
      new Label("Item name") { style = UiTheme.formLabelStyle },
      itemNameField,
      new Label("Category") { style = UiTheme.formLabelStyle },
      categoryField,
      new Label("Quantity") { style = UiTheme.formLabelStyle },
      quantityField,
      new Label("Minimum stock") { style = UiTheme.formLabelStyle },
      minimumStockField,
      new Label("Item type") { style = UiTheme.formLabelStyle },
      itemTypeBox,
      new Label("Expiry date") { style = UiTheme.formLabelStyle },
      expiryDatePicker,
      new VBox(12) {
        padding = Insets(12, 0, 0, 0)
        children = Seq(saveButton)
      }
    )

  private lazy val donationPanelCard: VBox = new VBox(16):
    prefWidth = 340
    minWidth = 340
    style = UiTheme.panelStyle
    children = Seq(UiTheme.sectionTitle("Log a donation"), donationForm, statusLabel)

  // ─── Inventory table card ──────────────────────────────────────────
  private lazy val tablePanel: VBox =
    val addStockButton = new Button("+1 Stock"):
      style = "-fx-background-color: #ffffff; -fx-text-fill: #166534; -fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #bbf7d0; -fx-border-radius: 6; -fx-cursor: hand;"
      onAction = _ => {
        Option(inventoryTable.selectionModel.value.selectedItem.value) match
          case Some(selected) =>
            pantryService.updateStockQuantity(selected.itemId, 1)
            refresh()
            displayStatus(s"Added +1 stock to '${selected.itemName}'. Updated total: ${selected.quantity + 1}.", isSuccess = true)
          case None =>
            val alert = new Alert(AlertType.Warning):
              title = "Selection Warning"
              headerText = "No Item Selected"
              contentText = "Please select an item from the table before updating stock."
            alert.showAndWait()
      }

    val reduceStockButton = new Button("-1 Stock"):
      style = "-fx-background-color: #ffffff; -fx-text-fill: #b45309; -fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #fef3c7; -fx-border-radius: 6; -fx-cursor: hand;"
      onAction = _ => {
        Option(inventoryTable.selectionModel.value.selectedItem.value) match
          case Some(selected) =>
            pantryService.updateStockQuantity(selected.itemId, -1)
            refresh()
            displayStatus(s"Reduced -1 stock from '${selected.itemName}'. Updated total: ${Math.max(0, selected.quantity - 1)}.", isSuccess = true)
          case None =>
            val alert = new Alert(AlertType.Warning):
              title = "Selection Warning"
              headerText = "No Item Selected"
              contentText = "Please select an item from the table before updating stock."
            alert.showAndWait()
      }

    val deleteItemButton = new Button("Delete Item"):
      style = UiTheme.destructiveButtonStyle
      onAction = _ => {
        Option(inventoryTable.selectionModel.value.selectedItem.value) match
          case Some(selected) =>
            val confirm = new Alert(AlertType.Confirmation):
              title = "Confirm Deletion"
              headerText = s"Delete '${selected.itemName}'?"
              contentText = s"Are you sure you want to permanently remove '${selected.itemName}' from inventory?"
            confirm.showAndWait().foreach {
              case ButtonType.OK =>
                pantryService.deleteItem(selected.itemId)
                refresh()
                displayStatus(s"Removed '${selected.itemName}' from inventory.", isSuccess = true)
              case _ => ()
            }
          case None =>
            val alert = new Alert(AlertType.Warning):
              title = "Selection Warning"
              headerText = "No Item Selected"
              contentText = "Please select an item from the inventory table before deleting."
            alert.showAndWait()
      }

    val searchField = new TextField:
      promptText = "🔍 Search inventory..."
      style = UiTheme.textFieldStyle
      maxWidth = Double.MaxValue

    searchField.text.onChange { (_, _, filterQuery) =>
      val query = Option(filterQuery).getOrElse("").trim.toLowerCase
      if query.isEmpty then
        refresh()
      else
        val filtered = pantryService.inventory.filter { item =>
          item.itemName.toLowerCase.contains(query) ||
          item.category.toLowerCase.contains(query) ||
          (item match {
            case _: PerishableItem => "perishable".contains(query)
            case _: NonPerishableItem => "non-perishable".contains(query)
          })
        }
        tableRows.setAll(filtered: _*)
    }

    val spacer = new scalafx.scene.layout.Region()
    HBox.setHgrow(spacer, Priority.Always)

    val topHeaderRow = new HBox(12):
      alignment = scalafx.geometry.Pos.CenterLeft
      children = Seq(
        UiTheme.sectionTitle("Current inventory"),
        spacer,
        addStockButton,
        reduceStockButton,
        deleteItemButton
      )

    val searchBarRow = new HBox():
      children = Seq(searchField)
      HBox.setHgrow(searchField, Priority.Always)

    new VBox(16):
      style = UiTheme.panelStyle
      children = Seq(topHeaderRow, searchBarRow, inventoryTable)
      VBox.setVgrow(inventoryTable, Priority.Always)

  private def inventoryColumn(
      headerText: String,
      valueOf: InventoryItem => String,
      columnWidth: Double,
      alignCenter: Boolean = false
  ): TableColumn[InventoryItem, String] =
    new TableColumn[InventoryItem, String]:
      text = headerText
      prefWidth = columnWidth
      cellValueFactory = tableCell => StringProperty(valueOf(tableCell.value))
      if alignCenter then style = "-fx-alignment: CENTER;"

  private def itemTypeLabel(item: InventoryItem): String =
    item match
      case _: PerishableItem => "Perishable"
      case _: NonPerishableItem => "Non-perishable"

  // ─── Form submission ───────────────────────────────────────────────
  private def submitItem(): Unit =
    val submission = for
      itemName <- InputValidator.requiredText("Item name", itemNameField.text.value)
      category <- InputValidator.requiredText("Category", categoryField.text.value)
      quantity <- InputValidator.positiveInt("Quantity", quantityField.text.value)
      minimumStockLevel <- InputValidator.positiveInt("Minimum stock", minimumStockField.text.value)
      item <- createInventoryItem(itemName, category, quantity, minimumStockLevel)
      _ <- pantryService.addInventory(item)
    yield item

    submission.fold(
      message => displayStatus(message, isSuccess = false),
      _ =>
        clearForm()
        refresh()
        displayStatus("Donation saved. Your inventory data is now persistent.", isSuccess = true)
    )

  private def createInventoryItem(
      itemName: String,
      category: String,
      quantity: Int,
      minimumStockLevel: Int
  ): Either[String, InventoryItem] =
    val generatedId = s"ITM-${System.currentTimeMillis()}"
    if itemTypeBox.value.value == "Perishable" then
      Option(expiryDatePicker.value.value).toRight("Expiry date is required for perishable items.").map { expiryDate =>
        new PerishableItem(generatedId, itemName, category, quantity, minimumStockLevel, LocalDate.now(), expiryDate)
      }
    else
      Right(new NonPerishableItem(generatedId, itemName, category, quantity, minimumStockLevel, LocalDate.now(), 24))

  private def clearForm(): Unit =
    itemNameField.clear()
    categoryField.clear()
    quantityField.clear()
    minimumStockField.clear()
    expiryDatePicker.value = null

  // ai-assisted: #13
  private def displayStatus(message: String, isSuccess: Boolean): Unit =
    statusLabel.text = message
    statusLabel.style = if isSuccess then UiTheme.statusSuccessStyle else UiTheme.statusErrorStyle
