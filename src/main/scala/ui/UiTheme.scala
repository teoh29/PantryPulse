package ui

import scalafx.scene.control.Button
import scalafx.scene.control.Label

// ai-assisted: #4
// why: AI assistance was used to provide CSS styles and ScalaFX layout code for modern card views.
object UiTheme:
  // ─── Modern Soft-Neo Lavender Palette ─────────────────────────────
  val primary = "#8C7CF0"
  val primaryGradientEnd = "#FFA6C9" // Soft pink for gradients
  val textDark = "#2D2B4A"
  val textGray = "#6C6A8B"
  val textMuted = "#A19EBF"
  val pageBg = "#FDFBFF" // Almost white grey-lavender
  val white = "#FFFFFF"

  val emphasisGreen = "#A5D6A7"
  val emphasisYellow = "#FFF59D"
  val emphasisOrange = "#FFCC80"

  // ─── Page & Panel Containers ───────────────────────────────────────
  val appBackground: String =
    s"-fx-background-color: $pageBg;"

  val navigationBackground: String =
    s"-fx-background-color: $pageBg; -fx-border-color: transparent #F0EEFF transparent transparent; -fx-border-width: 0 1 0 0;"

  val panelStyle: String =
    s"-fx-background-color: $white; -fx-background-radius: 20; -fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(140, 124, 240, 0.12), 32, 0, 0, 12); -fx-border-color: #EAE6FF; -fx-border-radius: 20; -fx-border-width: 1.5;"

  // ─── Typography System ─────────────────────────────────────────────
  val pageTitleStyle: String =
    s"-fx-text-fill: $textDark; -fx-font-size: 28px; -fx-font-weight: 900; -fx-letter-spacing: -0.5px;"

  val pageSubtitleStyle: String =
    s"-fx-text-fill: $textGray; -fx-font-size: 14px; -fx-font-weight: 400; -fx-line-spacing: 4px;"

  val formLabelStyle: String =
    s"-fx-text-fill: $textDark; -fx-font-size: 13px; -fx-font-weight: 700;"

  val metricValueStyle: String =
    s"-fx-text-fill: $primary; -fx-font-size: 36px; -fx-font-weight: 900; -fx-letter-spacing: -1px;"

  val metricLabelStyle: String =
    s"-fx-text-fill: $textGray; -fx-font-size: 13px; -fx-font-weight: 600;"

  // ─── Button Styles ─────────────────────────────────────────────────
  val primaryButtonStyle: String =
    s"-fx-background-color: linear-gradient(to right, $primary, $primaryGradientEnd); -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 14px; -fx-background-radius: 12; -fx-padding: 12 24; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(140,124,240,0.3), 12, 0, 0, 4);"

  val secondaryButtonStyle: String =
    s"-fx-background-color: #F0EEFF; -fx-text-fill: $primary; -fx-font-weight: 700; -fx-font-size: 13px; -fx-background-radius: 12; -fx-padding: 10 20; -fx-cursor: hand;"

  val destructiveButtonStyle: String =
    "-fx-background-color: #FFF0F0; -fx-text-fill: #E53935; -fx-font-weight: 700; -fx-font-size: 13px; -fx-background-radius: 12; -fx-padding: 10 20; -fx-cursor: hand;"

  // ─── Status Feedback Styles (Pastel Tints) ────────────────────────
  val statusSuccessStyle: String =
    "-fx-text-fill: #2E7D32; -fx-font-weight: 700; -fx-background-color: #E8F5E9; -fx-background-radius: 10; -fx-border-color: transparent; -fx-padding: 8 16; -fx-font-size: 13px;"

  val statusErrorStyle: String =
    "-fx-text-fill: #C62828; -fx-font-weight: 700; -fx-background-color: #FFEBEE; -fx-background-radius: 10; -fx-border-color: transparent; -fx-padding: 8 16; -fx-font-size: 13px;"

  val statusWarningStyle: String =
    "-fx-text-fill: #EF6C00; -fx-font-weight: 700; -fx-background-color: #FFF3E0; -fx-background-radius: 10; -fx-border-color: transparent; -fx-padding: 8 16; -fx-font-size: 13px;"

  // ─── Navigation Item Styles ────────────────────────────────────────
  val navigationDefaultStyle: String =
    s"-fx-background-color: transparent; -fx-text-fill: $textMuted; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 18; -fx-background-radius: 12; -fx-cursor: hand;"

  val navigationActiveStyle: String =
    s"-fx-background-color: #F0EEFF; -fx-text-fill: $primary; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-font-weight: 700; -fx-padding: 12 18; -fx-background-radius: 12; -fx-cursor: hand;"

  // ─── Form Controls ─────────────────────────────────────────────────
  val textFieldStyle: String =
    "-fx-background-color: #FFFFFF; -fx-border-color: #F0EEFF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(140, 124, 240, 0.02), 8, 0, 0, 2);"

  // ─── Component Helpers ─────────────────────────────────────────────
  def primaryButton(labelText: String): Button = new Button(labelText):
    style = primaryButtonStyle

  def secondaryButton(labelText: String): Button = new Button(labelText):
    style = secondaryButtonStyle

  def sectionTitle(labelText: String): Label = new Label(labelText):
    style = s"-fx-text-fill: $textDark; -fx-font-size: 18px; -fx-font-weight: 800; -fx-letter-spacing: -0.3px;"

  def sectionSubtitle(labelText: String): Label = new Label(labelText):
    style = s"-fx-text-fill: $textGray; -fx-font-size: 13px; -fx-wrap-text: true; -fx-line-spacing: 4px;"

  // ai-assisted: #15
  // why: AI assistance was used to design a reusable Toast banner notification component.
  val toastBannerStyle: String =
    "-fx-background-color: #2D2B4A; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 14px; -fx-padding: 16 24; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(140,124,240,0.15), 24, 0, 0, 8);"

  def toastLabel(message: String, isSuccess: Boolean): Label = new Label(message):
    style = if isSuccess then statusSuccessStyle else statusErrorStyle
