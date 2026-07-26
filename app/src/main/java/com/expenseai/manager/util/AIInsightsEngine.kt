package com.expenseai.manager.util

import com.expenseai.manager.domain.model.*
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AIInsightsEngine @Inject constructor() {

    fun generateInsights(
        expenses: List<Expense>,
        incomes: List<Income>,
        budgets: List<Budget>,
        currency: String
    ): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val filteredExpenses = expenses.filter { it.currency == currency && it.type == TransactionType.EXPENSE }

        insights.addAll(detectSpendingSpikes(filteredExpenses, currency))
        insights.addAll(detectBudgetWarnings(filteredExpenses, budgets, currency))
        insights.addAll(detectFrequentMerchants(filteredExpenses, currency))
        insights.addAll(detectRecurringPatterns(filteredExpenses, currency))
        insights.addAll(generateSavingsSuggestions(filteredExpenses, incomes, currency))
        insights.addAll(predictMonthlySpending(filteredExpenses, currency))
        insights.addAll(detectUnusualExpenses(filteredExpenses, currency))
        insights.addAll(detectPositiveStreak(filteredExpenses, budgets, currency))

        return insights.sortedByDescending { it.severity.ordinal }
    }

    private fun detectSpendingSpikes(expenses: List<Expense>, currency: String): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        cal.add(Calendar.MONTH, -1)
        val prevMonth = cal.get(Calendar.MONTH)
        val prevYear = cal.get(Calendar.YEAR)

        val currentMonthExpenses = expenses
            .filter { DateUtils.getMonth(it.date) == currentMonth + 1 && DateUtils.getYear(it.date) == currentYear }
            .sumOf { it.amount }

        val prevMonthExpenses = expenses
            .filter { DateUtils.getMonth(it.date) == prevMonth + 1 && DateUtils.getYear(it.date) == prevYear }
            .sumOf { it.amount }

        if (prevMonthExpenses > 0) {
            val changePercent = ((currentMonthExpenses - prevMonthExpenses) / prevMonthExpenses) * 100
            if (changePercent > 20) {
                insights.add(InsightData(
                    type = InsightType.SPENDING_SPIKE,
                    title = "Spending Spike Detected",
                    description = "Your spending this month is ${changePercent.toInt()}% higher than last month.",
                    actionable = "Review your ${DateUtils.getFullMonthName(currentMonth + 1)} expenses to identify areas to cut back.",
                    amount = currentMonthExpenses - prevMonthExpenses,
                    currency = currency,
                    severity = if (changePercent > 50) InsightSeverity.CRITICAL else InsightSeverity.WARNING
                ))
            }
        }
        return insights
    }

    private fun detectBudgetWarnings(
        expenses: List<Expense>,
        budgets: List<Budget>,
        currency: String
    ): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        budgets.filter { it.currency == currency && it.month == currentMonth && it.year == currentYear }
            .forEach { budget ->
                val spent = expenses
                    .filter { e ->
                        DateUtils.getMonth(e.date) == currentMonth &&
                        DateUtils.getYear(e.date) == currentYear &&
                        (budget.category == null || e.category == budget.category)
                    }
                    .sumOf { it.amount }

                val percentage = if (budget.amount > 0) spent / budget.amount else 0.0

                when {
                    percentage >= 1.0 -> insights.add(InsightData(
                        type = InsightType.BUDGET_EXCEEDED,
                        title = "Budget Exceeded: ${budget.name}",
                        description = "You've exceeded your ${CurrencyUtils.format(budget.amount, currency)} budget by ${CurrencyUtils.format(spent - budget.amount, currency)}.",
                        actionable = "Avoid further spending in this category for the rest of the month.",
                        amount = spent - budget.amount,
                        currency = currency,
                        severity = InsightSeverity.CRITICAL,
                        category = budget.category
                    ))
                    percentage >= budget.alertThreshold -> insights.add(InsightData(
                        type = InsightType.BUDGET_WARNING,
                        title = "Budget Alert: ${budget.name}",
                        description = "You've used ${(percentage * 100).toInt()}% of your ${CurrencyUtils.format(budget.amount, currency)} budget.",
                        actionable = "Only ${CurrencyUtils.format(budget.amount - spent, currency)} remaining. Spend carefully.",
                        amount = budget.amount - spent,
                        currency = currency,
                        severity = InsightSeverity.WARNING,
                        category = budget.category
                    ))
                }
            }
        return insights
    }

    private fun detectFrequentMerchants(expenses: List<Expense>, currency: String): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val thirtyDaysAgo = DateUtils.daysAgo(30)
        val recentExpenses = expenses.filter { it.date.after(thirtyDaysAgo) && it.merchant.isNotBlank() }

        val merchantCounts = recentExpenses.groupBy { it.merchant }
            .mapValues { (_, v) -> Pair(v.size, v.sumOf { it.amount }) }
            .filter { (_, v) -> v.first >= 4 }
            .toList()
            .sortedByDescending { (_, v) -> v.second }
            .take(3)

        merchantCounts.forEach { (merchant, data) ->
            insights.add(InsightData(
                type = InsightType.MERCHANT_FREQUENT,
                title = "Frequent: $merchant",
                description = "You've visited $merchant ${data.first} times in the last 30 days, spending ${CurrencyUtils.format(data.second, currency)}.",
                actionable = "Consider if all these visits are necessary.",
                amount = data.second,
                currency = currency,
                severity = InsightSeverity.INFO
            ))
        }
        return insights
    }

    private fun detectRecurringPatterns(expenses: List<Expense>, currency: String): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val ninetyDaysAgo = DateUtils.daysAgo(90)
        val recentExpenses = expenses.filter { it.date.after(ninetyDaysAgo) && it.merchant.isNotBlank() }

        recentExpenses.groupBy { it.merchant }
            .filter { (_, v) ->
                val amounts = v.map { it.amount }
                val avgAmount = amounts.average()
                v.size >= 3 && amounts.all { abs(it - avgAmount) / avgAmount < 0.15 }
            }
            .forEach { (merchant, txns) ->
                insights.add(InsightData(
                    type = InsightType.RECURRING_DETECTED,
                    title = "Possible Subscription: $merchant",
                    description = "Detected regular payments of ~${CurrencyUtils.format(txns.first().amount, currency)} to $merchant.",
                    actionable = "Add this as a recurring expense to track it automatically.",
                    amount = txns.first().amount,
                    currency = currency,
                    severity = InsightSeverity.INFO
                ))
            }
        return insights
    }

    private fun generateSavingsSuggestions(
        expenses: List<Expense>,
        incomes: List<Income>,
        currency: String
    ): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyIncome = incomes
            .filter { it.currency == currency && DateUtils.getMonth(it.date) == currentMonth && DateUtils.getYear(it.date) == currentYear }
            .sumOf { it.amount }

        val monthlyExpenses = expenses
            .filter { DateUtils.getMonth(it.date) == currentMonth && DateUtils.getYear(it.date) == currentYear }
            .sumOf { it.amount }

        if (monthlyIncome > 0) {
            val savingsRate = (monthlyIncome - monthlyExpenses) / monthlyIncome
            val targetSavings = monthlyIncome * 0.20

            if (savingsRate < 0.10) {
                insights.add(InsightData(
                    type = InsightType.SAVINGS_GOAL,
                    title = "Low Savings Rate",
                    description = "You're saving ${(savingsRate * 100).toInt()}% of income. Financial experts recommend at least 20%.",
                    actionable = "Try to save ${CurrencyUtils.format(targetSavings, currency)} this month by reducing discretionary spending.",
                    amount = targetSavings,
                    currency = currency,
                    severity = InsightSeverity.WARNING
                ))
            } else if (savingsRate >= 0.20) {
                insights.add(InsightData(
                    type = InsightType.SAVINGS_GOAL,
                    title = "Great Savings Rate!",
                    description = "You're saving ${(savingsRate * 100).toInt()}% of your income this month. Keep it up!",
                    actionable = "Consider investing your surplus in a high-yield savings or fixed deposit.",
                    severity = InsightSeverity.SUCCESS
                ))
            }

            val diningExpenses = expenses
                .filter { it.category == ExpenseCategory.FOOD_DINING && DateUtils.getMonth(it.date) == currentMonth }
                .sumOf { it.amount }

            if (diningExpenses > monthlyIncome * 0.15) {
                insights.add(InsightData(
                    type = InsightType.SAVING_OPPORTUNITY,
                    title = "Reduce Dining Out",
                    description = "Food & Dining accounts for ${((diningExpenses / monthlyIncome) * 100).toInt()}% of income (${CurrencyUtils.format(diningExpenses, currency)}).",
                    actionable = "Cooking at home 2-3 more days per week could save ~${CurrencyUtils.format(diningExpenses * 0.30, currency)}/month.",
                    amount = diningExpenses * 0.30,
                    currency = currency,
                    severity = InsightSeverity.INFO,
                    category = ExpenseCategory.FOOD_DINING
                ))
            }
        }
        return insights
    }

    private fun predictMonthlySpending(expenses: List<Expense>, currency: String): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val cal = Calendar.getInstance()
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        val spentSoFar = expenses
            .filter { DateUtils.getMonth(it.date) == currentMonth && DateUtils.getYear(it.date) == currentYear }
            .sumOf { it.amount }

        if (dayOfMonth >= 7 && spentSoFar > 0) {
            val dailyAvg = spentSoFar / dayOfMonth
            val predictedTotal = dailyAvg * daysInMonth
            insights.add(InsightData(
                type = InsightType.MONTHLY_PREDICTION,
                title = "Monthly Forecast",
                description = "Based on your current pace, you'll spend ~${CurrencyUtils.format(predictedTotal, currency)} this month.",
                actionable = if (predictedTotal > spentSoFar * 1.5) "You're on track to overspend. Consider slowing down." else "You're on a good pace.",
                amount = predictedTotal,
                currency = currency,
                severity = InsightSeverity.INFO
            ))
        }
        return insights
    }

    private fun detectUnusualExpenses(expenses: List<Expense>, currency: String): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        if (expenses.size < 5) return insights

        val avgAmount = expenses.map { it.amount }.average()
        val stdDev = kotlin.math.sqrt(expenses.map { (it.amount - avgAmount).let { d -> d * d } }.average())
        val threshold = avgAmount + (2.5 * stdDev)

        expenses
            .filter { it.amount > threshold && it.date.after(DateUtils.daysAgo(30)) }
            .sortedByDescending { it.amount }
            .take(2)
            .forEach { expense ->
                insights.add(InsightData(
                    type = InsightType.UNUSUAL_EXPENSE,
                    title = "Large Transaction",
                    description = "${expense.title} for ${CurrencyUtils.format(expense.amount, currency)} is unusually large.",
                    actionable = "Verify this transaction was intentional.",
                    amount = expense.amount,
                    currency = currency,
                    severity = InsightSeverity.WARNING,
                    category = expense.category
                ))
            }
        return insights
    }

    private fun detectPositiveStreak(
        expenses: List<Expense>,
        budgets: List<Budget>,
        currency: String
    ): List<InsightData> {
        val insights = mutableListOf<InsightData>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        // Check if under-budget for 3+ consecutive days
        val todayExpenses = expenses
            .filter { DateUtils.getMonth(it.date) == currentMonth && DateUtils.getYear(it.date) == currentYear }
            .sumOf { it.amount }

        val totalBudget = budgets
            .filter { it.currency == currency && it.month == currentMonth && it.year == currentYear }
            .sumOf { it.amount }

        if (totalBudget > 0 && todayExpenses < totalBudget * 0.70) {
            insights.add(InsightData(
                type = InsightType.POSITIVE_STREAK,
                title = "Well Under Budget",
                description = "You've only used ${((todayExpenses / totalBudget) * 100).toInt()}% of your monthly budget. Excellent discipline!",
                actionable = "Keep it up! Consider saving the surplus.",
                severity = InsightSeverity.SUCCESS
            ))
        }
        return insights
    }
}
