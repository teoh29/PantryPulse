# Third-Party Code & Asset Citations — Project_23051105

All external libraries, framework dependencies, and design assets utilized in this project are listed below in accordance with academic integrity requirements.

---

## 1. Third-Party Libraries & Frameworks

### ScalaFX
* **Description**: Scala DSL wrapper around JavaFX for building event-driven GUI applications.
* **URL**: https://www.scalafx.org/
* **License**: BSD 3-Clause License (https://opensource.org/licenses/BSD-3-Clause)
* **Usage**: Core UI framework used in `src/main/scala/ui/` and `src/main/scala/app/FoodPantryApp.scala`.

### OpenJFX (JavaFX 21 Binaries)
* **Description**: JavaFX graphics, controls, and FXML binaries for Windows, macOS, and Linux platforms.
* **URL**: https://openjfx.io/
* **License**: GPL v2 with Classpath Exception (https://openjdk.org/legal/gplv2+ce.html)
* **Usage**: Platform binaries configured in `build.sbt`.

### ScalaTest
* **Description**: Testing library for Scala language codebases.
* **URL**: https://www.scalatest.org/
* **License**: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
* **Usage**: Test suite dependency in `build.sbt`.

### OpenPDF
* **Description**: Open-source Java library for creating and modifying PDF files.
* **URL**: https://github.com/LibrePDF/OpenPDF
* **License**: LGPL / MPL (https://github.com/LibrePDF/OpenPDF?tab=readme-ov-file#license)
* **Usage**: Used in `AllocationEngine.scala` for exporting distribution plans.

---

## 2. Design Assets & Fonts

### System UI Font Stack
* **Description**: Native System CSS font stack (`-fx-font-family: 'Segoe UI', Helvetica, Arial, sans-serif`).
* **License**: Native OS System License / Free Web Usage.
* **Usage**: Configured in `src/main/resources/styles.css` and `src/main/scala/ui/UiTheme.scala`.
