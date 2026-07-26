package com.expenseai.manager

import com.expenseai.manager.domain.model.*
import com.expenseai.manager.util.AIInsightsEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class AIInsightsEngineTest {

    private lateinit var engine: AIInsightsEngine

    @Before
    fun setup() { engine = AIInsightsEngine() }

    @Test
    fun `generateInsights with empty data returns empty list`() {
        val insights = engine.generateInsights(emptyList(), emptyList(), emptyList(), "MYR")
        assertTrue(insights.isEmpty())
    }

    @Test
    fun `generateInsights detects budget warning when spending near limit`() {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val budget = Budget(
            id = 1, name = "Food Budget", amount = 1000.0, currency = "MYR",
            category = ExpenseCategory.FOOD_DINING, month = month, year = year, alertThreshold = 0.80
        )

        val expenses = (1..8).map {
            Expense(
                id = it.toLong(), title = "Lunch $it", amount = 100.0, currency = "MYR",
                category = ExpenseCategory.FOOD_DINING, date = Date(), paymentMethod = PaymentMethod.CASH
            )
        }

        val insights = engine.generateInsights(expenses, emptyList(), listOf(budget), "MYR")
        assertTrue(insights.any { it.type == InsightType.BUDGET_WARNING || it.type == InsightType.BUDGET_EXCEEDED })
    }

    @Test
    fun `generateInsights detects exceeded budget`() {
        val cal = Calendar.getInstance()
        val budget = Budget(
            id = 1, name = "Test", amount = 500.0, currency = "MYR",
            category = null, month = cal.get(Calendar.MONTH) + 1, year = cal.get(Calendar.YEAR)
        )
        val expenses = (1..6).map {
            Expense(
                id = it.toLong(), title = "Expense $it", amount = 100.0, currency = "MYR",
                category = ExpenseCategory.FOOD_DINING, date = Date(), paymentMethod = PaymentMethod.CASH
            )
        }
        val insights = engine.generateInsights(expenses, emptyList(), listOf(budget), "MYR")
        assertTrue(insights.any { it.type == InsightType.BUDGET_EXCEEDED })
    }

    @Test
    fun `generateInsights detects positive savings rate`() {
        val cal = Calendar.getInstance()
        val income = listOf(
            Income(id = 1, title = "Salary", amount = 5000.0, currency = "MYR",
                date = Date(), source = ExpenseCategory.SALARY)
        )
        val expenses = listOf(
            Expense(id = 1, title = "Groceries", amount = 500.0, currency = "MYR",
                category = ExpenseCategory.GROCERIES, date = Date(), paymentMethod = PaymentMethod.CASH)
        )
        val insights = engine.generateInsights(expenses, income, emptyList(), "MYR")
        assertTrue(insights.any { it.severity == InsightSeverity.SUCCESS })
    }
}
