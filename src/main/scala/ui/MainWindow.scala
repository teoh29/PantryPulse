package ui

import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.Priority
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.VBox
import scalafx.animation.FadeTransition
import scalafx.util.Duration
import service.DistributionPlanner
import service.PantryService

// ai-assisted: #9
// why: AI assistance was used to bind a reactive ScalaFX ObservableBuffer to an immutable Vector.
final class MainWindow(pantryService: PantryService):
  private val dashboardView = new DashboardView(pantryService)
  private val inventoryView = new InventoryView(pantryService)
  private val familyView = new FamilyView(pantryService)
  private val distributionView = new DistributionView(pantryService, new DistributionPlanner)
  private val aboutView = new AboutView()
  private val activeScreenProperty = StringProperty("Dashboard")
  private val contentHost = new StackPane:
    style = UiTheme.appBackground

  private val overviewButton = navigationButton("❖  Overview", "Dashboard")
  private val inventoryButton = navigationButton("📦  Inventory", "Inventory")
  private val familiesButton = navigationButton("👥  Families", "Families")
  private val distributionButton = navigationButton("✨  Distribution", "Distribution")
  private val aboutButton = navigationButton("ℹ  About System", "About")
  private val navigationButtons = Vector(
    "Dashboard" -> overviewButton,
    "Inventory" -> inventoryButton,
    "Families" -> familiesButton,
    "Distribution" -> distributionButton,
    "About" -> aboutButton
  )

  private val dynamicPageTitle = new Label("Operations Overview"):
    style = "-fx-text-fill: #2D2B4A; -fx-font-size: 16px; -fx-font-weight: 900; -fx-letter-spacing: -0.2px;"
    padding = Insets(2, 0, 0, 0)

  private val rightPanel = new BorderPane:
    top = applicationHeader
    center = contentHost
    style = UiTheme.appBackground

  val root: BorderPane = new BorderPane:
    left = navigationPanel
    center = rightPanel
    style = UiTheme.appBackground

  navigateTo("Dashboard")

  // ─── Header bar ────────────────────────────────────────────────────
  private def applicationHeader: HBox =
    val flexibleSpace = new HBox()
    HBox.setHgrow(flexibleSpace, Priority.Always)
    val syncBadge = new Label("● Live Sync"):
      style = "-fx-background-color: transparent; -fx-text-fill: #A5D6A7; -fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 4 10; -fx-border-color: #E8F5E9; -fx-border-radius: 10; -fx-background-radius: 10;"
    val adminBadge = new Label("Admin Manager"):
      style = "-fx-background-color: #F0EEFF; -fx-text-fill: #8C7CF0; -fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 10;"
    val aboutHeaderButton = new Button("ℹ  About System"):
      style = "-fx-background-color: transparent; -fx-text-fill: #6C6A8B; -fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 4 10; -fx-cursor: hand;"
      onAction = _ => { navigateTo("About") }

    new HBox(16):
      alignment = scalafx.geometry.Pos.CenterLeft
      padding = Insets(14, 32, 14, 32)
      style = "-fx-background-color: transparent; -fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;"
      children = Seq(
        dynamicPageTitle,
        flexibleSpace,
        syncBadge,
        adminBadge,
        aboutHeaderButton
      )

  // ─── Side navigation ───────────────────────────────────────────────
  private lazy val resetDataButton = new Button("🔄 Reset Benchmark Data"):
    maxWidth = Double.MaxValue
    style = "-fx-background-color: transparent; -fx-text-fill: #A19EBF; -fx-font-size: 11px; -fx-font-weight: 500; -fx-padding: 4 0; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;"
    onAction = _ => {
      val confirm = new scalafx.scene.control.Alert(scalafx.scene.control.Alert.AlertType.Confirmation):
        title = "Reset Benchmark Data"
        headerText = "Restore Original Sample Dataset?"
        contentText = "Are you sure you want to overwrite runtime CSV stores with clean benchmark sample data?"
      confirm.showAndWait().foreach {
        case scalafx.scene.control.ButtonType.OK =>
          pantryService.resetToSampleData().fold(
            err => System.err.println(s"[MainWindow] Reset failed: $err"),
            _ =>
              dashboardView.refresh()
              inventoryView.refresh()
              familyView.refresh()
              val alert = new scalafx.scene.control.Alert(scalafx.scene.control.Alert.AlertType.Information):
                title = "Data Restored"
                headerText = "Benchmark Sample Data Active"
                contentText = "Original sample inventory and family datasets have been restored successfully."
              alert.showAndWait()
          )
        case _ => ()
      }
    }

  private def navigationPanel: VBox =
    val flexibleSpace = new VBox()
    VBox.setVgrow(flexibleSpace, Priority.Always)
    val brandTitle = new Label("PantryPulse"):
      style = "-fx-text-fill: #2D2B4A; -fx-font-weight: 900; -fx-font-size: 24px; -fx-letter-spacing: -0.5px;"
    val brandIcon = new Label("✨"):
      style = "-fx-text-fill: #8C7CF0; -fx-font-size: 26px;"

    val brandBox = new HBox(8):
      alignment = scalafx.geometry.Pos.CenterLeft
      children = Seq(brandIcon, brandTitle)

    new VBox(12):
      prefWidth = 260
      padding = Insets(32, 24, 24, 24)
      style = UiTheme.navigationBackground
      children = Seq(
        brandBox,
        new Label(" ") { style = "-fx-font-size: 12px;" },
        overviewButton,
        inventoryButton,
        familiesButton,
        distributionButton,
        aboutButton,
        flexibleSpace,
        new VBox(8):
          padding = Insets(24, 0, 0, 0)
          style = "-fx-border-color: #F0EEFF transparent transparent transparent; -fx-border-width: 1 0 0 0;"
          children = Seq(
            resetDataButton
          )
      )

  private def navigationButton(labelText: String, screenName: String): Button =
    new Button(labelText):
      maxWidth = Double.MaxValue
      style = UiTheme.navigationDefaultStyle
      onAction = _ => { navigateTo(screenName) }

  // ─── Screen routing ────────────────────────────────────────────────
  private def navigateTo(screenName: String): Unit =
    activeScreenProperty.value = screenName
    screenName match
      case "Inventory" =>
        dynamicPageTitle.text = "Inventory & donations"
      case "Families" =>
        dynamicPageTitle.text = "Family demand"
      case "Distribution" =>
        dynamicPageTitle.text = "Daily distribution plan"
      case "About" =>
        dynamicPageTitle.text = "About PantryPulse"
      case _ =>
        dynamicPageTitle.text = "Operations Overview"

    navigationButtons.foreach { case (buttonScreenName, navigationButton) =>
      navigationButton.style =
        if buttonScreenName == activeScreenProperty.value then UiTheme.navigationActiveStyle
        else UiTheme.navigationDefaultStyle
    }
    dashboardView.refresh()
    val selectedView: Node = screenName match
      case "Inventory" =>
        inventoryView.refresh()
        inventoryView.root
      case "Families" =>
        familyView.refresh()
        familyView.root
      case "Distribution" => distributionView.root
      case "About" => aboutView.root
      case _ =>
        dashboardView.root
    
    // Apply smooth fade transition
    selectedView.opacity = 0.0
    contentHost.children.setAll(selectedView.delegate)
    
    val fade = new FadeTransition(Duration(250), selectedView)
    fade.fromValue = 0.0
    fade.toValue = 1.0
    fade.play()

type MainView = MainWindow
