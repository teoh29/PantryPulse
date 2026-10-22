name := "FoodPantrySystem"
version := "0.1.0-SNAPSHOT"

scalaVersion := "3.3.6"

// Compiles strictly with -Wunused flag required by Rubric S1-2
scalacOptions ++= Seq(
  "-feature",
  "-Wunused:all",
  "-Wconf:cat=deprecation:s"
)

libraryDependencies ++= Seq(
  // ScalaFX 21 UI Library
  "org.scalafx" %% "scalafx" % "21.0.0-R32",

  // JavaFX binaries for cross-platform compatibility
  "org.openjfx" % "javafx-controls" % "21" classifier "win",
  "org.openjfx" % "javafx-controls" % "21" classifier "mac",
  "org.openjfx" % "javafx-controls" % "21" classifier "linux",
  "org.openjfx" % "javafx-fxml" % "21" classifier "win",
  "org.openjfx" % "javafx-fxml" % "21" classifier "mac",
  "org.openjfx" % "javafx-fxml" % "21" classifier "linux",

  // Unit testing (optional but recommended)
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  
  // PDF Generation for reports
  "com.github.librepdf" % "openpdf" % "1.3.38"
)

Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat