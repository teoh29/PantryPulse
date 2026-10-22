import java.nio.file.Paths
import repository.FileDataStore
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import service.PantryService
import ui.MainWindow

object Main extends JFXApp3:
  override def start(): Unit =
    val dataStore = new FileDataStore(Paths.get("data", "inventory.csv"), Paths.get("data", "families.csv"))
    val pantryService = new PantryService(dataStore)
    val mainWindow = new MainWindow(pantryService)

    val appScene = new Scene(1280, 800):
      root = mainWindow.root

    val cssResource = Option(Thread.currentThread().getContextClassLoader.getResource("styles.css"))
      .orElse(Option(getClass.getResource("/styles.css")))

    cssResource.foreach { url =>
      appScene.stylesheets.add(url.toExternalForm)
    }

    stage = new JFXApp3.PrimaryStage:
      title = "PantryPulse | Food Pantry Inventory & Demand Management"
      minWidth = 1050
      minHeight = 700
      maximized = true
      scene = appScene
