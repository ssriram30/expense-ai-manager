package com.expenseai.manager.domain.model

data class InsightData(
    val type: InsightType,
    val title: String,
    val description: String,
    val actionable: String = "",
    val amount: Double? = null,
    val currency: String? = null,
    val severity: InsightSeverity = InsightSeverity.INFO,
    val category: ExpenseCategory? = null
)

enum class InsightType {
    SPENDING_SPIKE,
    BUDGET_WARNING,
    BUDGET_EXCEEDED,
    SAVING_OPPORTUNITY,
    RECURRING_DETECTED,
    MERCHANT_FREQUENT,
    MONTHLY_PREDICTION,
    SAVINGS_GOAL,
    UNUSUAL_EXPENSE,
    CATEGORY_TREND,
    POSITIVE_STREAK
}

enum class InsightSeverity {
    SUCCESS, INFO, WARNING, CRITICAL
}
