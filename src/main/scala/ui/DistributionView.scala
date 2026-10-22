package ui

import java.time.LocalDate
import model.Allocation
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.control.TableColumn
import scalafx.scene.control.TableView
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.VBox
import service.DistributionPlanner
import service.PantryService

import scalafx.scene.control.ScrollPane

final class DistributionView(pantryService: PantryService, distributionPlanner: DistributionPlanner):
  private val allocationRows = ObservableBuffer.empty[Allocation]
  
  private val allocationTable = new TableView[Allocation](allocationRows):
    prefHeight = 420
    columnResizePolicy = TableView.ConstrainedResizePolicy
    style = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;"
    placeholder = new Label("No allocation plan generated yet. Click 'Generate today's plan' above."):
      style = "-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13px;"
    columns ++= Seq(
      allocationColumn("Household", _.householdName, 200, alignCenter = true),
      allocationColumn("Item Allocated", _.itemName, 180, alignCenter = true),
      allocationColumn("Units", allocation => s"${allocation.quantity} units", 100, alignCenter = true),
      allocationColumn("Priority Score", allocation => allocation.priorityScore.toString, 120, alignCenter = true)
    )

  private val summaryLabel = new Label("Generate a plan to match the most urgent inventory to the highest-priority households."):
    wrapText = true
    maxWidth = Double.MaxValue
    style = UiTheme.pageSubtitleStyle

  private val generateButton = new scalafx.scene.control.Button("Generate today's plan"):
    style = UiTheme.primaryButtonStyle
    minWidth = 170
    defaultButton = true
    onAction = _ => { generatePlan() }

  private val fulfillButton = new scalafx.scene.control.Button("✓ Fulfill & Deduct Stock"):
    style = "-fx-background-color: #166534; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 14px; -fx-background-radius: 12; -fx-padding: 12 24; -fx-cursor: hand;"
    minWidth = 170
    disable = true
    onAction = _ => { fulfillPlan() }

  private val exportButton = new scalafx.scene.control.Button("📁 Export Plan (PDF)"):
    style = "-fx-background-color: #ffffff; -fx-text-fill: #2563eb; -fx-font-weight: 700; -fx-font-size: 14px; -fx-background-radius: 12; -fx-border-color: #2563eb; -fx-border-radius: 12; -fx-padding: 11 23; -fx-cursor: hand;"
    minWidth = 160
    disable = true
    onAction = _ => { exportPlanPdf() }

  private val pageContent = new VBox(48):
    padding = Insets(0, 0, 0, 0)
    children = Seq(
      new VBox(16):
        style = UiTheme.panelStyle
        children = Seq(
          UiTheme.sectionTitle("Plan controls"),
          new HBox(16) {
            children = Seq(generateButton, fulfillButton, exportButton)
          },
          summaryLabel
        ),
      new VBox(16):
        style = UiTheme.panelStyle
        children = Seq(UiTheme.sectionTitle("Recommended allocations"), allocationTable)
    )

  private val pageLayout = new VBox(24):
    padding = Insets(8, 32, 48, 24)
    children = Seq(pageContent)

  private val mainScroll = new ScrollPane:
    content = pageLayout
    fitToWidth = true
    style = "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"

  val root: BorderPane = new BorderPane:
    center = mainScroll
    style = UiTheme.appBackground

  private def allocationColumn(
      headerText: String,
      valueOf: Allocation => String,
      columnWidth: Double,
      alignCenter: Boolean = false
  ): TableColumn[Allocation, String] =
    new TableColumn[Allocation, String]:
      text = headerText
      prefWidth = columnWidth
      cellValueFactory = tableCell => StringProperty(valueOf(tableCell.value))
      if alignCenter then style = "-fx-alignment: CENTER;"

  private def generatePlan(): Unit =
    val distributionPlan = distributionPlanner.createPlan(pantryService.inventory, pantryService.families, LocalDate.now())
    allocationRows.setAll(distributionPlan.allocations: _*)
    
    if distributionPlan.allocations.isEmpty then
      summaryLabel.text = "No allocation can be produced yet. Add at least one inventory item and one recipient family."
      summaryLabel.style = UiTheme.statusErrorStyle
      fulfillButton.disable = true
      exportButton.disable = true
    else
      summaryLabel.text = s"${distributionPlan.allocations.size} allocations prepared: ${distributionPlan.totalAllocatedUnits} units recommended."
      summaryLabel.style = UiTheme.statusSuccessStyle
      fulfillButton.disable = false
      exportButton.disable = false

  private def fulfillPlan(): Unit =
    val currentAllocations = allocationRows.toVector
    if currentAllocations.nonEmpty then
      pantryService.fulfillPlan(currentAllocations).fold(
        errorMessage =>
          summaryLabel.text = errorMessage
          summaryLabel.style = UiTheme.statusErrorStyle,
        _ =>
          summaryLabel.text = s"Plan fulfilled successfully! Stock allocated to ${currentAllocations.size} households and inventory updated."
          summaryLabel.style = UiTheme.statusSuccessStyle
          allocationRows.clear()
          fulfillButton.disable = true
          exportButton.disable = true
      )

  private def exportPlanPdf(): Unit =
    val currentAllocations = allocationRows.toVector
    if currentAllocations.nonEmpty then
      val plan = model.DistributionPlan(s"PLAN-${System.currentTimeMillis()}", LocalDate.now(), currentAllocations)
      pantryService.exportPlanToPdf(plan).fold(
        err =>
          summaryLabel.text = err
          summaryLabel.style = UiTheme.statusErrorStyle,
        filePath =>
          summaryLabel.text = s"Plan exported successfully to: $filePath"
          summaryLabel.style = UiTheme.statusSuccessStyle
      )
