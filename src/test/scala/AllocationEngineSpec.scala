import java.time.LocalDate
import model.NonPerishableItem
import model.PerishableItem
import model.RecipientFamily
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import service.DistributionPlanner
import service.InputValidator

// ai-assisted: #1
// why: AI assistance was used to write automated unit tests covering domain priorities, allocation rules, and input validation.
class AllocationEngineSpec extends AnyFunSuite with Matchers:

  test("PerishableItem calculates correct priority score based on days until expiry") {
    val today = LocalDate.now()
    val expiringToday = new PerishableItem("P1", "Fresh Milk", "Dairy", 10, 5, today.minusDays(1), today)
    val expiringSoon = new PerishableItem("P2", "Yogurt", "Dairy", 10, 5, today.minusDays(1), today.plusDays(2))
    val normalItem = new PerishableItem("P3", "Cheese", "Dairy", 20, 5, today.minusDays(1), today.plusDays(10))

    expiringToday.priorityScore shouldBe 100
    expiringSoon.priorityScore shouldBe 90
    normalItem.priorityScore shouldBe 50
  }

  test("NonPerishableItem calculates priority based on low stock status") {
    val today = LocalDate.now()
    val lowStock = new NonPerishableItem("NP1", "Canned Beans", "Canned", 2, 5, today.minusDays(10), 12)
    val normalStock = new NonPerishableItem("NP2", "Rice", "Grains", 20, 5, today.minusDays(10), 24)

    lowStock.priorityScore shouldBe 70
    normalStock.priorityScore shouldBe 40
  }

  test("DistributionPlanner allocates items to highest priority families") {
    val today = LocalDate.now()
    val item1 = new PerishableItem("P1", "Milk", "Dairy", 10, 2, today.minusDays(1), today)
    val item2 = new NonPerishableItem("NP1", "Rice", "Grains", 20, 5, today.minusDays(10), 12)

    val familyHigh = RecipientFamily("F1", "Smith Family", 4, BigDecimal(1500), Set("Halal"), 5)
    val familyLow = RecipientFamily("F2", "Jones Family", 2, BigDecimal(3500), Set.empty, 2)

    val planner = new DistributionPlanner()
    val plan = planner.createPlan(Vector(item1, item2), Vector(familyHigh, familyLow), today)

    plan.allocations should not be empty
    plan.allocations.head.familyId shouldBe "F1"
  }

  test("InputValidator enforces non-empty text and numeric bounds") {
    InputValidator.requiredText("Item Name", "") shouldBe Left("Item Name is required.")
    InputValidator.requiredText("Item Name", "   ") shouldBe Left("Item Name is required.")
    InputValidator.requiredText("Item Name", "Valid Name") shouldBe Right("Valid Name")

    InputValidator.positiveInt("Quantity", "-5") shouldBe Left("Quantity must be greater than zero.")
    InputValidator.positiveInt("Quantity", "10") shouldBe Right(10)

    InputValidator.nonNegativeDecimal("Income", "-100") shouldBe Left("Income cannot be negative.")
    InputValidator.nonNegativeDecimal("Income", "1500.50") shouldBe Right(BigDecimal(1500.50))
  }
