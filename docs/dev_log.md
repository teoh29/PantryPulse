# Development Log — Project_23051105

## 2026-07-20
- **Phase**: Requirements Analysis & Architecture Sketching.
- **Activity**: Analyzed UN SDG Goal 1 (No Poverty) sub-domains and selected Food Pantry Inventory & Demand Allocation.
- **Design Decisions**: Outlined immutable domain model (`InventoryItem`, `PerishableItem`, `NonPerishableItem`, `RecipientFamily`, `DistributionPlan`) and generic repository trait (`Repository[T <: Identifiable]`).

## 2026-07-22
- **Phase**: Core Domain & Persistence Layer Implementation.
- **Activity**: Implemented domain classes with traits (`Identifiable`, `Expirable`) and polymorphic priority scoring.
- **Details**: Built `FileDataStore` using monadic `scala.util.Try`/`Either` error handling to load and save inventory items to CSV format (`data/inventory.csv`).

## 2026-07-24
- **Phase**: Service Layer & Algorithmic Planner.
- **Activity**: Developed `PantryService`, `ValidationUtils`, and `AllocationEngine`.
- **Details**: Implemented defensive form validation and priority-matching distribution algorithms balancing food shelf life (`ChronoUnit.DAYS`) against family need level.

## 2026-07-25
- **Phase**: ScalaFX User Interface Development.
- **Activity**: Built `MainWindow`, `DashboardView`, `InventoryView`, `FamilyView`, and `DistributionView`.
- **Details**: Applied card-based layouts, custom CSS styling (`UiTheme`), responsive ScalaFX controls (`TableView`, `ComboBox`, `DatePicker`), and reactive property bindings.

## 2026-07-27
- **Phase**: Refactoring, Compiler Strictness & Documentation.
- **Activity**: Cleaned unused imports and variables to achieve 0 compiler warnings under `-Wunused:all`.
- **Details**: Tagged AI-assisted blocks with `// ai-assisted: #N` and rationale comments; compiled complete submission docs, AI interaction logs, and manifest.

## 2026-07-28
- **Phase**: Assessment Enhancements, PDF Export, Analytics Modal & Defensive UX.
- **Activity**: Added PDF distribution plan export (`exportPlanToPdf`), Operations Analytics Summary modal (`showAnalyticsModal`), dynamic status badges, one-click sample data reset (`resetToSampleData`), and defensive deletion confirmation popups.
- **Details**: Fulfills Criteria S1-3, S1-18, and S4-1 to S4-6; updated `ai/interaction_log.md` with entries #11–#15 and source code dual tags (`// ai-assisted: #N`).

## 2026-07-31
- **Phase**: Automated Unit Testing & Domain Edge Case Validation.
- **Activity**: Implemented ScalaTest suite (`AllocationEngineSpec.scala`) covering item priority scoring, distribution planner matching, and input validator bounds.
- **Details**: Verified `PerishableItem` expiration scoring, `NonPerishableItem` low-stock rules, and `ValidationUtils` error responses under `sbt test`.

## 2026-08-04
- **Phase**: Fallback Dataset Recovery & Defensive IO Hardening.
- **Activity**: Refactored `FileDataStore.scala` with fallback storage paths (`fallbackFamilyFile` and `fallbackInventoryFile`).
- **Details**: Ensured seamless cold-boot application startup when primary CSV storage directories are missing or uninitialized.

## 2026-08-08
- **Phase**: Rubric Compliance & API Import Audit.
- **Activity**: Refactored direct `javafx` imports to `scalafx` wrappers (`DashboardView.scala`), renamed single-letter parameters (`AllocationEngine.scala`), and expanded AI logs.
- **Details**: Fulfills Criteria S1-5 and S2-8; added Entry #17 to `ai/interaction_log.md` and tagged source lines with `// ai-assisted: #17`.

## 2026-08-09
- **Phase**: Submission Finalization, Reflections & Build Verification.
- **Activity**: Refined personal reflection (`docs/reflection.md`), AI integration essay (`docs/ai_reflection.md`), updated UML diagram (`docs/UML.png`), and synchronized submission metrics.
- **Details**: Verified clean compilation (`sbt clean compile`), confirmed zero warning output, and updated `submission_manifest.md`.
