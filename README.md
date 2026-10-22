# PantryPulse — Food Pantry Inventory & Demand Management System

**Student Name:** Teoh En Yi 
**Student ID:** 23051105  
**Course Code:** PRG2104 Object-Oriented Programming  
**Academic Session:** 2026 Semester 2 — Final Project (Tier C — AI-Integrated)  
**Theme:** UN SDG Goal 1 — No Poverty  
**Sub-domain:** Food Pantry Inventory & Demand Allocation  

---

## 1. Project Overview

**PantryPulse** is a standalone ScalaFX desktop application designed to support community food pantries and non-governmental aid organisations serving vulnerable, low-income households. By balancing inventory shelf life, stock levels, and household socio-economic urgency, the system generates transparent, data-driven daily distribution plans that minimize food waste and ensure equitable aid delivery.

### Key Features
1. **Dashboard & Low-Stock Alerts**: Real-time summary cards displaying stock metrics, immediate low-stock alert badges, and interactive inventory quick-search filtering.
2. **Inventory & Donation Logging**: Interactive entry form for perishable and non-perishable food donations with persistent CSV file storage across application restarts.
3. **Recipient Family Demand Register**: Household enrollment capturing family size, monthly income, dietary requirements, and assigned urgency levels.
4. **Automated Priority Distribution Planner**: Algorithmic matching module prioritizing expiring/low-stock inventory to highest-need households based on household size and urgency scores.

---

## 2. Requirements & Development Environment

* **Java Development Kit (JDK)**: Java 21 (LTS)
* **Build Tool**: sbt 1.9+ (or sbt 1.12+)
* **Scala Version**: Scala 3.3.6
* **GUI Framework**: ScalaFX 21.0.0-R32 (JavaFX 21 binaries)
* **Testing Library**: ScalaTest 3.2.18

---

## 3. How to Run the Application

### Compiling the Project cleanly (0 warnings under `-Wunused:all`)
```bash
sbt clean compile
```

### Launching the Graphical User Interface
```bash
sbt run
```

### Running Automated Unit Tests
```bash
sbt test
```

---

## 4. Architecture & OOP Highlights

* **Inheritance & Subtype Polymorphism**: `abstract class InventoryItem` extended by concrete subclasses `PerishableItem` and `NonPerishableItem`, overriding polymorphic `priorityScore` calculations.
* **Parametric Polymorphism**: Generic repository abstraction `trait Repository[T <: Identifiable]` implemented by `InMemoryRepository[T]`.
* **Immutability & Encapsulation**: Domain models strictly use immutable collections (`Vector`, `Set`) and `val` parameters. Encapsulated fields utilize explicit rationale comments.
* **Defensive Exception Handling**: All file IO and string parsing operations in `FileDataStore` and `InputValidator` are safely wrapped in `scala.util.Try` or `Either[String, T]`.
* **Reactive ScalaFX UI**: Modern card layout utilizing reactive `ObjectProperty`, `StringProperty`, and `ObservableBuffer` controls.

---

## 5. Project Directory Structure

```text
Project_23051105/
├── build.sbt                          # Scala 3.3+, ScalaFX 21, ScalaTest
├── README.md                          # Setup, run commands, architecture summary
├── submission_manifest.md             # Checklist & metrics signed
├── .gitignore
│
├── ai/                                # AI Usage Documentation
│   ├── declaration.md                 # Signed Template 3
│   └── interaction_log.md             # Template 2 (>= 10 entries)
│
├── data/                              # CSV Persistence Storage
│   ├── distribution_plan.csv
│   ├── families.csv
│   └── inventory.csv
│
├── docs/                              # Deliverables
│   ├── UML.png                        # Class diagram (>= 5 classes)
│   ├── ai_reflection.md               # AI reflection essay (300-500 words)
│   ├── citations.md                   # Citations & licenses
│   ├── dev_log.md                     # >= 5 dated entries
│   ├── reflection.md                  # Personal reflection (350-700 words)
│   └── demo.mp4                       # <= 5 min video walkthrough
│
├── project/
│   └── build.properties               # sbt version
│
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── styles.css             # UI styling
    │   │
    │   └── scala/
    │       ├── Main.scala             # Entry point (extends JFXApp3)
    │       │
    │       ├── model/                 # Domain Models (Subtype Polymorphism)
    │       │   ├── DistributionPlan.scala
    │       │   ├── FamilyRequest.scala
    │       │   └── PantryItem.scala   # PerishableItem & NonPerishableItem
    │       │
    │       ├── repository/            # Data Access (Generic Repositories)
    │       │   ├── FileRepositories.scala
    │       │   └── Repository.scala   # Generic trait [T <: Identifiable]
    │       │
    │       ├── service/               # Business Logic
    │       │   ├── AllocationEngine.scala
    │       │   ├── SampleData.scala
    │       │   └── ValidationUtils.scala
    │       │
    │       ├── traits/                # Shared Interfaces
    │       │   ├── Expirable.scala
    │       │   └── Identifiable.scala
    │       │
    │       └── ui/                    # 4 Distinct Screens + Main Shell
    │           ├── DashboardView.scala    # Screen 1: Metrics & Low Stock
    │           ├── DistributionView.scala # Screen 2: Plan Generator & CSV Export
    │           ├── FamilyView.scala       # Screen 3: Beneficiary Household Registry
    │           ├── InventoryView.scala    # Screen 4: Donation Logging & Stock Table
    │           ├── MainWindow.scala       # Root Window Shell & Sidebar
    │           └── UiTheme.scala          # Design tokens & styles
    │
    └── test/
        └── scala/
            └── AllocationEngineSpec.scala # Unit tests
```

---

## 6. Data Storage Architecture Rationale

* **Runtime Writable Persistence (`./data/`)**: Live user edits, donations, and family enrollments persist dynamically to `data/inventory.csv` and `data/families.csv` at the project root. This enables reliable, writable file storage across application restarts (since files packaged under `src/main/resources/` become read-only assets in packaged builds).
* **Packaged Seed Data (`src/main/resources/data/`)**: Seed copies of `inventory.csv` and `families.csv` are maintained under `src/main/resources/data/`. The `FileDataStore` automatically falls back to loading these seed resources if the root `./data/` directory is not yet initialized.

---

## 7. AI Usage Summary

This project was developed under the PRG2104 **Tier C (AI-Integrated)** policy. AI assistance (Claude / Gemini) was leveraged for iterative code structure optimization, ScalaFX layout styling, and defensive validation logic. Every prompt, response summary, and code modification is fully logged in `ai/interaction_log.md` and declared in `ai/declaration.md`.

