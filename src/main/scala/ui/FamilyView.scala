package ui

import model.RecipientFamily
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Button
import scalafx.scene.control.ButtonType
import scalafx.scene.control.ComboBox
import scalafx.scene.control.Label
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.TableColumn
import scalafx.scene.control.TableView
import scalafx.scene.control.TextField
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.Priority
import scalafx.scene.layout.Region
import scalafx.scene.layout.VBox
import scala.util.Try
import service.InputValidator
import service.PantryService

final class FamilyView(pantryService: PantryService):
  private val householdNameField = new TextField:
    promptText = "e.g. Tan Household"
    style = UiTheme.textFieldStyle
  private val householdSizeField = new TextField:
    promptText = "e.g. 4"
    style = UiTheme.textFieldStyle
  private val incomeField = new TextField:
    promptText = "e.g. 1800.00"
    style = UiTheme.textFieldStyle

  householdSizeField.text.onChange { (_, _, newValue) =>
    if newValue != null && !newValue.matches("\\d*") then
      householdSizeField.text = newValue.replaceAll("[^0-9]", "")
  }
  incomeField.text.onChange { (_, _, newValue) =>
    if newValue != null && !newValue.matches("[0-9.]*") then
      incomeField.text = newValue.replaceAll("[^0-9.]", "")
  }

  private val dietaryField = new TextField:
    promptText = "e.g. Halal, Vegetarian (comma separated)"
    style = UiTheme.textFieldStyle
  private val priorityBox = new ComboBox[String](
    ObservableBuffer("1 - Low urgency", "2 - Medium urgency", "3 - Standard need", "4 - High urgency", "5 - Critical urgency")
  ):
    value = "3 - Standard need"
    maxWidth = Double.MaxValue
  private val statusLabel = new Label():
    wrapText = true

  private val familyRows = ObservableBuffer.empty[RecipientFamily]
  private val familyTable = new TableView[RecipientFamily](familyRows):
    prefHeight = 380
    minHeight = 250
    columnResizePolicy = TableView.ConstrainedResizePolicy
    style = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;"
    placeholder = new Label("No registered households found."):
      style = "-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13px;"
    columns ++= Seq(
      familyColumn("Household", _.householdName, 180, alignCenter = true),
      familyColumn("Size", family => family.householdSize.toString, 60, alignCenter = true),
      familyColumn("Monthly Income", family => s"RM ${family.monthlyIncome}", 110, alignCenter = true),
      familyColumn("Dietary", family => family.dietaryRequirements.mkString(", "), 140, alignCenter = true),
      familyColumn("Need level", family => family.priorityLevel.toString, 80, alignCenter = true)
    )

  private val saveButton = UiTheme.primaryButton("Register family")
  saveButton.defaultButton = true
  saveButton.maxWidth = Double.MaxValue
  saveButton.onAction = _ => { submitFamily() }

  private val mainContent = new HBox(24):
    padding = Insets(0, 0, 0, 0)
    children = Seq(registrationPanelCard, familyTablePanel)

  HBox.setHgrow(familyTablePanel, Priority.Always)

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

  // Initial load of CSV-persisted families into TableView
  refresh()

  def refresh(): Unit =
    Try {
      val currentFamilies = pantryService.families
      familyRows.setAll(currentFamilies: _*)
    }.failed.foreach { ex =>
      System.err.println(s"[FamilyView] Error refreshing family table: ${ex.getMessage}")
    }

  // ─── Registration card ─────────────────────────────────────────────
  private lazy val registrationPanelCard: VBox = new VBox(16):
    prefWidth = 340
    minWidth = 340
    style = UiTheme.panelStyle
    children = Seq(UiTheme.sectionTitle("Register a recipient"), registrationForm, statusLabel)

  private lazy val registrationForm: VBox = new VBox(14):
    children = Seq(
      new Label("Household name") { style = UiTheme.formLabelStyle },
      householdNameField,
      new Label("Household size") { style = UiTheme.formLabelStyle },
      householdSizeField,
      new Label("Monthly income (RM)") { style = UiTheme.formLabelStyle },
      incomeField,
      new Label("Dietary requirements") { style = UiTheme.formLabelStyle },
      dietaryField,
      new Label("Priority level") { style = UiTheme.formLabelStyle },
      priorityBox,
      new VBox(12) {
        padding = Insets(12, 0, 0, 0)
        children = Seq(saveButton)
      }
    )

  // ─── Households table card ─────────────────────────────────────────
  private lazy val familyTablePanel: VBox =
    val deleteFamilyButton = new Button("Delete Household"):
      style = UiTheme.destructiveButtonStyle
      onAction = _ => {
        Option(familyTable.selectionModel.value.selectedItem.value) match
          case Some(selected) =>
            val confirm = new Alert(AlertType.Confirmation):
              title = "Confirm Deletion"
              headerText = s"Delete '${selected.householdName}'?"
              contentText = s"Are you sure you want to permanently remove '${selected.householdName}' from the family registry?"
            confirm.showAndWait().foreach {
              case ButtonType.OK =>
                pantryService.deleteFamily(selected.familyId)
                refresh()
                displayStatus(s"Removed '${selected.householdName}' from family registry.", isSuccess = true)
              case _ => ()
            }
          case None =>
            val alert = new Alert(AlertType.Warning):
              title = "Selection Warning"
              headerText = "No Household Selected"
              contentText = "Please select a household from the table before deleting."
            alert.showAndWait()
      }

    val searchField = new TextField:
      promptText = "🔍 Search households..."
      style = UiTheme.textFieldStyle
      maxWidth = Double.MaxValue

    searchField.text.onChange { (_, _, filterQuery) =>
      val query = Option(filterQuery).getOrElse("").trim.toLowerCase
      if query.isEmpty then
        refresh()
      else
        val filtered = pantryService.families.filter { family =>
          family.householdName.toLowerCase.contains(query) ||
          family.dietaryRequirements.exists(_.toLowerCase.contains(query)) ||
          family.priorityLevel.toString.contains(query)
        }
        familyRows.setAll(filtered: _*)
    }

    val spacer = new Region()
    HBox.setHgrow(spacer, Priority.Always)

    val topHeaderRow = new HBox(12):
      alignment = Pos.CenterLeft
      children = Seq(
        UiTheme.sectionTitle("Registered households"),
        spacer,
        deleteFamilyButton
      )

    val searchBarRow = new HBox():
      children = Seq(searchField)
      HBox.setHgrow(searchField, Priority.Always)

    new VBox(16):
      style = UiTheme.panelStyle
      children = Seq(topHeaderRow, searchBarRow, familyTable)
      VBox.setVgrow(familyTable, Priority.Always)

  private def familyColumn(
      headerText: String,
      valueOf: RecipientFamily => String,
      columnWidth: Double,
      alignCenter: Boolean = false
  ): TableColumn[RecipientFamily, String] =
    new TableColumn[RecipientFamily, String]:
      text = headerText
      prefWidth = columnWidth
      cellValueFactory = cell => StringProperty(valueOf(cell.value))
      if alignCenter then style = "-fx-alignment: CENTER;"

  // ─── Form submission ───────────────────────────────────────────────
  private def submitFamily(): Unit =
    val submission = for
      householdName <- InputValidator.requiredText("Household name", householdNameField.text.value)
      householdSize <- InputValidator.positiveInt("Household size", householdSizeField.text.value)
      monthlyIncome <- InputValidator.nonNegativeDecimal("Monthly income", incomeField.text.value)
      family <- createFamily(householdName, householdSize, monthlyIncome)
      _ <- pantryService.addFamily(family)
    yield family

    submission.fold(
      message => displayStatus(message, isSuccess = false),
      registeredFamily =>
        clearForm()
        Try {
          familyRows += registeredFamily
          refresh()
        }.failed.foreach { ex =>
          System.err.println(s"[FamilyView] Exception updating table after registration: ${ex.getMessage}")
        }
        displayStatus("Family registered successfully.", isSuccess = true)
    )

  private def createFamily(
      householdName: String,
      householdSize: Int,
      monthlyIncome: BigDecimal
  ): Either[String, RecipientFamily] =
    val dietaryRequirements = dietaryField.text.value.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSet
    val priorityLevel = priorityBox.value.value.take(1).toIntOption.getOrElse(3)
    Right(
      RecipientFamily(
        s"FAM-${System.currentTimeMillis()}",
        householdName,
        householdSize,
        monthlyIncome,
        dietaryRequirements,
        priorityLevel
      )
    )

  private def clearForm(): Unit =
    householdNameField.clear()
    householdSizeField.clear()
    incomeField.clear()
    dietaryField.clear()
    priorityBox.value = "3 - Standard need"

  private def displayStatus(message: String, isSuccess: Boolean): Unit =
    statusLabel.text = message
    statusLabel.style = if isSuccess then UiTheme.statusSuccessStyle else UiTheme.statusErrorStyle
