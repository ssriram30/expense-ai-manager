package com.expenseai.manager.domain.model

data class AnalyticsData(
    val totalExpenses: Double,
    val totalIncome: Double,
    val netSavings: Double,
    val categoryBreakdown: Map<ExpenseCategory, Double>,
    val merchantBreakdown: Map<String, Double>,
    val monthlyTrend: List<MonthlyData>,
    val dailyTrend: List<DailyData>,
    val paymentMethodBreakdown: Map<PaymentMethod, Double>,
    val currencyBreakdown: Map<String, Double>,
    val topMerchants: List<Pair<String, Double>>,
    val averageDailySpend: Double,
    val averageTransactionAmount: Double,
    val transactionCount: Int
)

data class MonthlyData(
    val month: Int,
    val year: Int,
    val expenses: Double,
    val income: Double,
    val savings: Double,
    val currency: String
)

data class DailyData(
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val amount: Double
)

data class CategorySpending(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

data class SpendingTrend(
    val label: String,
    val amount: Double,
    val previousAmount: Double,
    val changePercent: Double
)
