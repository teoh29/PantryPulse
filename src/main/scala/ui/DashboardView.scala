package ui

import model.InventoryItem
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.chart.BarChart
import scalafx.scene.chart.CategoryAxis
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.PieChart
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Button
import scalafx.scene.control.ComboBox
import scalafx.scene.control.Label
import scalafx.scene.control.ProgressBar
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.TableColumn
import scalafx.scene.control.TableView
import scalafx.scene.control.TextField
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.Priority
import scalafx.scene.layout.Region
import scalafx.scene.layout.VBox
import service.PantryService

// ai-assisted: #17
// why: AI assistance was used to implement dynamic search/filtering and safe batch restocking handlers with selection validation.
final class DashboardView(pantryService: PantryService):
  private val stockUnitLabel = new Label()
  private val lowStockLabel = new Label()
  private val familyLabel = new Label()
  private val priorityItemLabel = new Label()

  // ─── Health Progress Bar & Indicators ──────────────────────────────
  private val healthProgressBar = new ProgressBar():
    progress = 0.85
    prefWidth = 240
    style = s"-fx-accent: ${UiTheme.emphasisGreen}; -fx-control-inner-background: #F8F7FF; -fx-background-color: transparent;"
  private val healthStatusText = new Label("85% Inventory Health Score"):
    style = "-fx-text-fill: #2E7D32; -fx-font-weight: 800; -fx-font-size: 13px;"

  // ─── Table View Data & Setup ───────────────────────────────────────
  private val attentionRows = ObservableBuffer.empty[InventoryItem]
  private val attentionTable = new TableView[InventoryItem](attentionRows):
    prefHeight = 220
    columnResizePolicy = TableView.ConstrainedResizePolicy
    style = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;"
    placeholder = new VBox(8):
      alignment = Pos.Center
      children = Seq(
        new Label("No inventory items found") { styleClass += "placeholder-title" },
        new Label("Try adjusting your search query or category filters.") { styleClass += "placeholder-subtitle" }
      )
    columns ++= Seq(
      attentionColumn("ITEM ID", _.itemId, alignCenter = true),
      attentionColumn("ITEM NAME", _.itemName, alignCenter = true),
      attentionColumn("CATEGORY", _.category, alignCenter = true),
      attentionColumn("STOCK / MIN", item => s"${item.quantity} / ${item.minimumStockLevel}", alignCenter = true),
      attentionColumn("PRIORITY SCORE", _.priorityScore.toString, alignCenter = true),
      attentionColumn("STATUS", item => if item.quantity == 0 then "OUT OF STOCK" else if item.isLowStock then "LOW STOCK" else "OPTIMAL", alignCenter = true)
    )

  // ─── Search & Filter Controls ─────────────────────────────────────
  private val searchField = new TextField:
    promptText = "Search by item name or ID..."
    style = UiTheme.textFieldStyle
    prefWidth = 240
  private val searchCategoryBox = new ComboBox[String](ObservableBuffer("All Categories", "Dairy", "Produce", "Staples", "Protein")):
    value = "All Categories"
  private val filterModeBox = new ComboBox[String](ObservableBuffer("All Attention Items", "Low Stock Only", "High Priority (>=90)")):
    value = "All Attention Items"
  private val searchButton = new Button("Search"):
    prefWidth = 100
    style = s"-fx-background-color: linear-gradient(to right, ${UiTheme.primary}, ${UiTheme.primaryGradientEnd}); -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 14; -fx-cursor: hand;"

  // ─── Interactive Visual Charts ─────────────────────────────────────
  private val categoryPieChart = new PieChart():
    legendVisible = false
    labelsVisible = false
    prefHeight = 140
    minHeight = 140
    prefWidth = 140
    maxWidth = 140
    padding = Insets(0)

  private val categoryLegendBox = new VBox(12):
    alignment = Pos.CenterLeft
    padding = Insets(0, 0, 0, 32)
    minWidth = Region.USE_PREF_SIZE

  private val donutHole = new scalafx.scene.shape.Circle():
    radius = 30
    fill = scalafx.scene.paint.Color.web("#ffffff")

  private val donutChartContainer = new scalafx.scene.layout.StackPane():
    children = Seq(categoryPieChart, donutHole)

  private val customDonutChart = new HBox(16):
    alignment = Pos.Center
    padding = Insets(24, 0, 0, 0)
    children = Seq(donutChartContainer, categoryLegendBox)

  private val xAxis = new CategoryAxis():
    label = "Item"
  private val yAxis = new NumberAxis():
    label = "Units"

  private val stockBarChart = new BarChart[String, Number](xAxis, yAxis):
    title = ""
    animated = false
    prefHeight = 200
    minHeight = 200
    maxWidth = Double.MaxValue

  // ─── Scrollable Root Layout ───────────────────────────────────────
  private val mainContentBox = new VBox(24):
    padding = Insets(8, 32, 48, 24)
    style = "-fx-background-color: transparent;"
    maxWidth = Double.MaxValue
    children = Seq(
      headerBanner,
      metricRow,
      chartVisualisationRow,
      healthSummaryStrip,
      lowStockPanel
    )

  private val scrollPane = new ScrollPane:
    content = mainContentBox
    fitToWidth = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style = "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"

  val root: BorderPane = new BorderPane:
    style = UiTheme.appBackground
    center = scrollPane

  // ─── Event Handlers & Real-Time Dynamic Listeners ─────────────────
  searchButton.onAction = _ => { applyFilters() }
  searchField.text.onChange { (_, _, _) => applyFilters() }
  searchCategoryBox.value.onChange { (_, _, _) => applyFilters() }
  filterModeBox.value.onChange { (_, _, _) => applyFilters() }

  refresh()

  def refresh(): Unit =
    val inventoryItems = pantryService.inventory
    val recipientFamilies = pantryService.families
    val lowStockItems = inventoryItems.filter(_.isLowStock)
    val priorityItems = inventoryItems.filter(item => item.isLowStock || item.priorityScore >= 90)

    // Update KPI metric values
    val totalUnits = inventoryItems.map(_.quantity).sum
    stockUnitLabel.text = totalUnits.toString
    lowStockLabel.text = lowStockItems.size.toString
    val highNeedCount = recipientFamilies.count(_.isHighNeed)
    familyLabel.text = recipientFamilies.size.toString
    if highNeedCount > 0 then
      val highNeedBadge = new Label(s"$highNeedCount High Need"):
        style = "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-weight: 700;"
      familyLabel.graphic = highNeedBadge
      familyLabel.contentDisplay = scalafx.scene.control.ContentDisplay.Right
      familyLabel.graphicTextGap = 10
    else
      familyLabel.graphic = null
    
    priorityItemLabel.text = priorityItems.size.toString

    // Update Health Score Ratio
    val healthyItemsCount = inventoryItems.count(!_.isLowStock)
    val healthRatio = if inventoryItems.nonEmpty then healthyItemsCount.toDouble / inventoryItems.size.toDouble else 1.0
    healthProgressBar.progress = healthRatio
    val percentInt = (healthRatio * 100).toInt
    healthStatusText.text = s"$percentInt% Inventory Health Score"

    // Update Category PieChart Data
    val categoryTotals = inventoryItems.groupBy(_.category).toVector.sortBy(_._1)
    val totalAllQty = categoryTotals.map(_._2.map(_.quantity).sum).sum
    val pieSlices = categoryTotals.map { case (category, items) =>
      val totalQty = items.map(_.quantity).sum
      new javafx.scene.chart.PieChart.Data(s"$category", totalQty.toDouble)
    }
    categoryPieChart.delegate.getData.setAll(pieSlices: _*)

    val pieColors = Array("#8C7CF0", "#4A90E2", "#50E3C2", "#F5A623", "#E65100", "#9B9B9B")
    
    val legendItems = categoryTotals.zipWithIndex.map { case ((category, items), index) =>
      val totalQty = items.map(_.quantity).sum
      val percentage = if totalAllQty > 0 then (totalQty.toDouble / totalAllQty * 100) else 0.0
      val pctStr = f"$percentage%.1f%%"
      
      val dot = new scalafx.scene.shape.Circle():
        radius = 5
        fill = scalafx.scene.paint.Color.web(pieColors(index % pieColors.length))
      val nameLbl = new Label(category):
        style = "-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-weight: 500;"
        minWidth = Region.USE_PREF_SIZE
      val pctLbl = new Label(pctStr):
        style = "-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: 700;"
        minWidth = Region.USE_PREF_SIZE
      
      val spacer = new Region()
      HBox.setHgrow(spacer, Priority.Always)
      
      new HBox(16):
        alignment = Pos.CenterLeft
        children = Seq(dot, nameLbl, spacer, pctLbl)
    }
    categoryLegendBox.children = legendItems

    // Add tooltips and custom colors to pie slices via runLater to ensure nodes are created
    scalafx.application.Platform.runLater {
      val dataList = categoryPieChart.delegate.getData
      (0 until dataList.size()).foreach { index =>
        val data = dataList.get(index)
        val node = data.getNode
        if node != null then
          node.setStyle(s"-fx-pie-color: ${pieColors(index % pieColors.length)};")
          scalafx.scene.control.Tooltip.install(node: scalafx.scene.Node, new scalafx.scene.control.Tooltip(s"${data.getName}: ${data.getPieValue.toInt} units"))
      }
    }

    // Update Stock Comparison BarChart Data
    val displayItems = inventoryItems.take(7)
    val currentSeries = new javafx.scene.chart.XYChart.Series[String, Number]()
    currentSeries.setName("Current Stock")

    val minSeries = new javafx.scene.chart.XYChart.Series[String, Number]()
    minSeries.setName("Min Threshold")

    displayItems.foreach { item =>
      currentSeries.getData.add(new javafx.scene.chart.XYChart.Data[String, Number](item.itemName, item.quantity))
      minSeries.getData.add(new javafx.scene.chart.XYChart.Data[String, Number](item.itemName, item.minimumStockLevel))
    }
    stockBarChart.delegate.getData.clear()
    stockBarChart.delegate.getData.addAll(currentSeries, minSeries)

    scalafx.application.Platform.runLater {
      stockBarChart.delegate.getData.forEach { series =>
        series.getData.forEach { data =>
          val node = data.getNode
          if node != null then
            scalafx.scene.control.Tooltip.install(node: scalafx.scene.Node, new scalafx.scene.control.Tooltip(s"${data.getXValue}: ${data.getYValue} units"))
        }
      }
    }

    applyFilters()

  def applyFilters(): Unit =
    val query = searchField.text.value.toLowerCase.trim
    val categoryFilter = searchCategoryBox.value.value
    val modeFilter = filterModeBox.value.value
    val allItems = pantryService.inventory

    val filtered = allItems.filter { item =>
      val matchesQuery = query.isEmpty || item.itemName.toLowerCase.contains(query) || item.itemId.toLowerCase.contains(query)
      val matchesCategory = categoryFilter == "All Categories" || item.category.equalsIgnoreCase(categoryFilter)
      val matchesMode = modeFilter match
        case "Low Stock Only" => item.isLowStock
        case "High Priority (>=90)" => item.priorityScore >= 90
        case _ => item.isLowStock || item.priorityScore >= 90 || query.nonEmpty

      matchesQuery && matchesCategory && matchesMode
    }
    attentionRows.setAll(filtered: _*)

  private def handleRestock(amount: Int): Unit =
    Option(attentionTable.selectionModel.value.selectedItem.value) match
      case Some(selected) =>
        pantryService.updateStockQuantity(selected.itemId, amount)
        refresh()
      case None =>
        val alert = new Alert(AlertType.Warning):
          headerText = "No Item Selected"
          contentText = "Please select an item from the table above before restocking."
        alert.showAndWait()

  // ─── Component Construction Helpers ───────────────────────────────
  // ai-assisted: #12
  // why: AI assistance was used to compute functional aggregate metrics (foldLeft/groupBy/partition) and render an Analytics Breakdown modal.
  private def showAnalyticsModal(): Unit =
    val items = pantryService.inventory
    val families = pantryService.families
    val (perishables, nonPerishables) = items.partition {
      case _: model.PerishableItem => true
      case _ => false
    }

    val totalItemsCount = items.size
    val totalStockQty = items.foldLeft(0)(_ + _.quantity)
    val avgHouseholdSize = if families.nonEmpty then families.foldLeft(0)(_ + _.householdSize).toDouble / families.size.toDouble else 0.0
    val avgMonthlyIncome = if families.nonEmpty then families.foldLeft(BigDecimal(0))(_ + _.monthlyIncome) / BigDecimal(families.size) else BigDecimal(0)

    val contentStr =
      s"""• Total Registered Items: $totalItemsCount
         |• Total Stock Quantity: $totalStockQty units
         |• Perishable Items: ${perishables.size} (${if totalItemsCount > 0 then (perishables.size * 100 / totalItemsCount) else 0}%)
         |• Non-Perishable Items: ${nonPerishables.size} (${if totalItemsCount > 0 then (nonPerishables.size * 100 / totalItemsCount) else 0}%)
         |
         |• Registered Households: ${families.size}
         |• Average Household Size: ${"%.1f".format(avgHouseholdSize)} members
         |• Average Household Income: RM ${"%.2f".format(avgMonthlyIncome)}
         |""".stripMargin

    val alert = new Alert(AlertType.Information):
      title = "Pantry Analytics Summary"
      headerText = "📊 Operations & Demand Statistics"
      contentText = contentStr
    alert.showAndWait()

  private lazy val headerBanner: HBox =
    val featureBadges = new HBox(8):
      alignment = Pos.CenterLeft
      padding = Insets(8, 0, 0, 0)
      children = Seq(
        new Label("⚡ Live Inventory") { style = "-fx-background-color: #F8F7FF; -fx-text-fill: #2D2B4A; -fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 12; -fx-background-radius: 8;" },
        new Label("📊 Stock Analytics") { style = "-fx-background-color: #F8F7FF; -fx-text-fill: #2D2B4A; -fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 12; -fx-background-radius: 8;" },
        new Label("👥 Demand Allocation") { style = "-fx-background-color: #F8F7FF; -fx-text-fill: #2D2B4A; -fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 12; -fx-background-radius: 8;" }
      )

    val analyticsButton = new Button("📊 Analytics Summary"):
      style = UiTheme.secondaryButtonStyle
      onAction = _ => { showAnalyticsModal() }

    val spacer = new Region()
    HBox.setHgrow(spacer, Priority.Always)

    new HBox(12):
      alignment = Pos.CenterLeft
      children = Seq(featureBadges, spacer, analyticsButton)

  private lazy val metricRow: HBox =
    val stockCard = metricCard("Total Stock Units", stockUnitLabel, UiTheme.primary, "#F0EEFF", "📦")
    val attentionCard = metricCard("Low Stock Alerts", lowStockLabel, "#EF6C00", "#FFF3E0", "⚠")
    val familyCard = metricCard("Registered Families", familyLabel, UiTheme.primary, "#F0EEFF", "👥")
    val priorityCard = metricCard("Priority Items", priorityItemLabel, "#C62828", "#FFEBEE", "🔥")

    HBox.setHgrow(stockCard, Priority.Always)
    HBox.setHgrow(attentionCard, Priority.Always)
    HBox.setHgrow(familyCard, Priority.Always)
    HBox.setHgrow(priorityCard, Priority.Always)

    new HBox(20):
      children = Seq(stockCard, attentionCard, familyCard, priorityCard)

  private def metricCard(
      titleText: String,
      valueLabel: Label,
      iconColorHex: String,
      iconBgHex: String,
      iconStr: String
  ): VBox =
    valueLabel.style = s"-fx-text-fill: #0f172a; -fx-font-size: 24px; -fx-font-weight: 800;"
    valueLabel.wrapText = true

    val titleLbl = new Label(titleText):
      style = "-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 600;"
      wrapText = true

    val iconLabel = new Label(iconStr):
      style = s"-fx-text-fill: $iconColorHex; -fx-font-size: 12px; -fx-font-weight: 700; -fx-background-color: $iconBgHex; -fx-background-radius: 6; -fx-padding: 2 6;"

    val topRow = new HBox(8):
      alignment = Pos.CenterLeft
      val titleSpacer = new Region()
      HBox.setHgrow(titleSpacer, Priority.Always)
      children = Seq(titleLbl, titleSpacer, iconLabel)

    val valueRow = new HBox(12):
      alignment = scalafx.geometry.Pos.BaselineLeft
      children = Seq(valueLabel)

    new VBox(12):
      styleClass += "metric-card"
      style = UiTheme.panelStyle + " -fx-padding: 14 20;"
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
      prefWidth = 250
      minWidth = 0
      children = Seq(topRow, valueRow)

  private lazy val chartVisualisationRow: HBox =
    val pieBox = new VBox(8):
      style = UiTheme.panelStyle
      children = Seq(
        UiTheme.sectionTitle("Stock Category Distribution"),
        customDonutChart
      )

    val barBox = new VBox(8):
      style = UiTheme.panelStyle
      children = Seq(
        UiTheme.sectionTitle("Stock vs Minimum Threshold"),
        stockBarChart
      )

    pieBox.maxWidth = 340
    barBox.maxWidth = 480

    new HBox(20):
      children = Seq(pieBox, barBox)

  private lazy val healthSummaryStrip: HBox =
    val labelTitle = new Label("Pantry Inventory Health Score:"):
      style = "-fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-font-weight: 700;"

    val flexSpace = new Region()
    HBox.setHgrow(flexSpace, Priority.Always)

    new HBox(16):
      alignment = Pos.CenterLeft
      style = UiTheme.panelStyle
      children = Seq(
        new Label("📊"):
          style = "-fx-font-size: 20px;",
        labelTitle,
        healthProgressBar,
        healthStatusText,
        flexSpace,
        new Label("⚡ Freshness Index: 92%"):
          style = "-fx-text-fill: #6C6A8B; -fx-font-size: 12px; -fx-font-weight: 700;"
      )

  private lazy val lowStockPanel: VBox =
    val titleLabel = UiTheme.sectionTitle("Low Stock & Attention Items")
    val badgeLabel = new Label("⚠ Action Required"):
      styleClass += "action-required-badge"
    val headerSpacer = new Region()
    HBox.setHgrow(headerSpacer, Priority.Always)
    val headerBox = new HBox(10):
      alignment = Pos.CenterLeft
      children = Seq(titleLabel, badgeLabel, headerSpacer)

    val resetButton = new Button("Reset Filter"):
      prefWidth = 100
      style = "-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 14; -fx-cursor: hand;"
      onAction = _ => {
        searchField.clear()
        searchCategoryBox.value = "All Categories"
        filterModeBox.value = "All Attention Items"
        refresh()
      }

    val searchSpacer = new Region()
    HBox.setHgrow(searchSpacer, Priority.Always)
    val controlsBox = new HBox(10):
      alignment = Pos.CenterLeft
      children = Seq(searchField, searchCategoryBox, filterModeBox, searchSpacer, searchButton, resetButton)
      HBox.setHgrow(searchField, Priority.Always)

    val quickRestock5Button = new Button("+5 Quick Restock"):
      style = "-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;"
      onAction = _ => { handleRestock(5) }

    val quickRestock10Button = new Button("+10 Bulk Restock"):
      style = "-fx-background-color: linear-gradient(to right, #6366f1, #4f46e5); -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;"
      onAction = _ => { handleRestock(10) }

    val footerBox = new HBox(10):
      alignment = Pos.CenterRight
      padding = Insets(6, 0, 0, 0)
      children = Seq(
        new Label("Select an item in table above then click restock:"):
          style = "-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-alignment: CENTER;",
        quickRestock5Button,
        quickRestock10Button
      )

    new VBox(16):
      style = UiTheme.panelStyle
      children = Seq(headerBox, controlsBox, attentionTable, footerBox)

  private def attentionColumn(
      headerText: String,
      valueOf: InventoryItem => String,
      alignCenter: Boolean = false
  ): TableColumn[InventoryItem, String] =
    new TableColumn[InventoryItem, String]:
      text = headerText
      cellValueFactory = tableCell => StringProperty(valueOf(tableCell.value))
      if alignCenter then style = "-fx-alignment: CENTER;"

  // searchPanel was moved to lowStockPanel
