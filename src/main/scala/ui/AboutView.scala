package ui

import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.Priority
import scalafx.scene.layout.VBox

// ai-assisted: #16
// why: AI assistance was used to apply the Modern Soft-Neo Lavender UI design system across AboutView cards and hero banners.
final class AboutView:

  // ─── 1. Brand Hero Banner (Soft Lavender Pastel Gradient) ──────────
  private val heroBrandTitle = new Label("PantryPulse"):
    style = "-fx-text-fill: #ffffff; -fx-font-size: 32px; -fx-font-weight: 800; -fx-letter-spacing: -1.0px; -fx-font-family: 'Segoe UI', sans-serif;"

  private val heroTagline = new Label("Zero-Waste Food Logistics & Algorithmic Relief Operations"):
    style = "-fx-text-fill: #F0EEFF; -fx-font-size: 14px; -fx-font-weight: 600; -fx-font-family: 'Segoe UI', sans-serif;"

  private val heroDescription = new Label(
    "Engineering a dignified, data-driven food pantry ecosystem. " +
      "PantryPulse pairs real-time inventory tracking with priority-weighted recipient matching " +
      "to eliminate perishable food waste and serve low-income households under UN SDG-1."
  ):
    style = "-fx-text-fill: #FDFBFF; -fx-font-size: 12px; -fx-line-spacing: 5px; -fx-wrap-text: true; -fx-font-family: 'Segoe UI', sans-serif;"

  private val heroComposition = new VBox(16):
    padding = Insets(48, 48, 48, 48)
    style = s"-fx-background-color: linear-gradient(to bottom right, ${UiTheme.primary}, ${UiTheme.primaryGradientEnd}); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(140,124,240,0.3), 24, 0, 0, 8);"
    children = Seq(heroBrandTitle, heroTagline, heroDescription)

  // ─── 2. Key Purpose Cards (Soft Floating Cards) ────────────────────
  private def sectionBlock(titleText: String, subtitleText: String, bodyText: String, accentBg: String, accentText: String): VBox =
    val titleLabel = new Label(titleText):
      style = "-fx-text-fill: #2D2B4A; -fx-font-size: 15px; -fx-font-weight: 800; -fx-letter-spacing: -0.2px; -fx-font-family: 'Segoe UI', sans-serif;"
    val subLabel = new Label(subtitleText):
      style = s"-fx-background-color: $accentBg; -fx-text-fill: $accentText; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 5 10; -fx-background-radius: 8; -fx-font-family: 'Segoe UI', sans-serif;"
    val bodyLabel = new Label(bodyText):
      style = "-fx-text-fill: #6C6A8B; -fx-font-size: 12px; -fx-line-spacing: 5px; -fx-wrap-text: true; -fx-font-family: 'Segoe UI', sans-serif;"

    new VBox(16):
      padding = Insets(32)
      maxWidth = Double.MaxValue
      style = UiTheme.panelStyle
      children = Seq(subLabel, titleLabel, bodyLabel)

  private val missionBlock = sectionBlock(
    "🎯  Mission & Operational Purpose",
    "EQUITABLE AID ALLOCATION",
    "To empower community food pantries with algorithmic precision. By transforming raw donation streams " +
      "into prioritized relief packages, PantryPulse guarantees that every family receives aid matched " +
      "to their household size, income tier, and urgent dietary requirements with complete dignity.",
    "#F0EEFF",
    "#5A48D0"
  )

  private val visionBlock = sectionBlock(
    "🌱  Vision & Environmental Impact",
    "ZERO-WASTE EXPIRY LOGISTICS",
    "A zero-waste local food ecosystem where expiring perishable goods are dispatched immediately to families " +
      "experiencing high need, eliminating localized hunger while upholding data privacy, operational resilience, " +
      "and transparent community stewardship.",
    "#E8F5E9",
    "#2E7D32"
  )

  private val dualGrid = new HBox(16):
    children = Seq(missionBlock, visionBlock)

  HBox.setHgrow(missionBlock, Priority.Always)
  HBox.setHgrow(visionBlock, Priority.Always)

  // ─── 3. Core Technical Capabilities ────────────────────────────────
  private def capabilityItem(titleText: String, detailText: String): VBox =
    val titleLabel = new Label(titleText):
      style = "-fx-text-fill: #2D2B4A; -fx-font-size: 14px; -fx-font-weight: 700; -fx-letter-spacing: -0.1px; -fx-font-family: 'Segoe UI', sans-serif;"
    val detailLabel = new Label(detailText):
      style = "-fx-text-fill: #6C6A8B; -fx-font-size: 12px; -fx-wrap-text: true; -fx-line-spacing: 4px; -fx-font-family: 'Segoe UI', sans-serif;"

    new VBox(10):
      padding = Insets(24)
      maxWidth = Double.MaxValue
      style = UiTheme.panelStyle
      children = Seq(titleLabel, detailLabel)

  private val cap1 = capabilityItem("⚡ Priority Dispatch", "Dynamic algorithmic scoring combining household size, income tier thresholds, and emergency vulnerability flags.")
  private val cap2 = capabilityItem("🔄 Dynamic Rotation", "Precedence scoring for expiring perishable items (PerishableItem) ensuring zero-waste inventory flow.")
  private val cap3 = capabilityItem("🔒 Local Integrity", "Atomic CSV storage (FileDataStore) with fallback dataset recovery and monadic Try/Either exception handling.")

  private val capabilityGrid = new HBox(20):
    children = Seq(cap1, cap2, cap3)

  HBox.setHgrow(cap1, Priority.Always)
  HBox.setHgrow(cap2, Priority.Always)
  HBox.setHgrow(cap3, Priority.Always)

  // ─── 4. Technical Architecture Specifications ──────────────────────
  private def specTag(label: String, valText: String): HBox =
    val keyL = new Label(s"$label:"):
      style = "-fx-text-fill: #6C6A8B; -fx-font-size: 11px; -fx-font-weight: 600; -fx-font-family: 'Segoe UI', sans-serif;"
    val valL = new Label(valText):
      style = s"-fx-text-fill: ${UiTheme.primary}; -fx-font-size: 11px; -fx-font-weight: 700; -fx-font-family: 'Segoe UI', sans-serif;"
    new HBox(6):
      alignment = Pos.CenterLeft
      padding = Insets(8, 16, 8, 16)
      style = "-fx-background-color: #F0EEFF; -fx-background-radius: 10; -fx-border-color: transparent;"
      children = Seq(keyL, valL)

  private val techSpecsRow = new HBox(12):
    alignment = Pos.CenterLeft
    children = Seq(
      specTag("Language", "Scala 3.3.6"),
      specTag("UI Engine", "ScalaFX 21"),
      specTag("Architecture", "3-Tier Model-View-Service"),
      specTag("Status", "Clean Zero-Warning Build")
    )

  private val specsSection = new VBox(16):
    padding = Insets(32)
    style = UiTheme.panelStyle
    children = Seq(
      UiTheme.sectionTitle("🛠  Technical Architecture Specifications"),
      UiTheme.sectionSubtitle("Built strictly following object-oriented principles, immutable collections, and monadic error handling."),
      techSpecsRow
    )

  // ─── Main View Assembly ────────────────────────────────────────────
  private val pageLayout = new VBox(32):
    padding = Insets(8, 32, 48, 24)
    children = Seq(
      heroComposition,
      dualGrid,
      UiTheme.sectionTitle("Core Technical Capabilities"),
      capabilityGrid,
      specsSection
    )

  private val mainScroll = new ScrollPane:
    content = pageLayout
    fitToWidth = true
    style = "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"

  val root: BorderPane = new BorderPane:
    center = mainScroll
    style = UiTheme.appBackground
