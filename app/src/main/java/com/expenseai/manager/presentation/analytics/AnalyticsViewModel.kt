package com.expenseai.manager.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.*
import com.expenseai.manager.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedCurrency: String = "MYR",
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val categoryBreakdown: List<Pair<ExpenseCategory, Double>> = emptyList(),
    val merchantBreakdown: List<Pair<String, Double>> = emptyList(),
    val monthlyTrend: List<MonthlyData> = emptyList(),
    val dailyTrend: List<DailyData> = emptyList(),
    val paymentBreakdown: List<Pair<PaymentMethod, Double>> = emptyList(),
    val myrExpenses: Double = 0.0,
    val inrExpenses: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTransaction: Double = 0.0
)

enum class AnalyticsPeriod(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val incomeRepo: IncomeRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _period = MutableStateFlow(AnalyticsPeriod.THIS_MONTH)
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepo.getAllExpenses(),
                incomeRepo.getAllIncomes(),
                prefsDataStore.userPreferences,
                _period
            ) { expenses, incomes, prefs, period ->
                val currency = prefs.defaultCurrency
                val (startDate, endDate) = getPeriodDates(period)

                val filteredExpenses = expenses.filter { e ->
                    e.type == TransactionType.EXPENSE &&
                    e.currency == currency &&
                    e.date.time >= startDate.time &&
                    e.date.time <= endDate.time
                }

                val filteredIncome = incomes.filter { i ->
                    i.currency == currency &&
                    i.date.time >= startDate.time &&
                    i.date.time <= endDate.time
                }

                val totalExpenses = filteredExpenses.sumOf { it.amount }
                val totalIncome = filteredIncome.sumOf { it.amount }

                val categoryBreakdown = filteredExpenses
                    .groupBy { it.category }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { (_, v) -> v }

                val merchantBreakdown = filteredExpenses
                    .filter { it.merchant.isNotBlank() }
                    .groupBy { it.merchant }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { (_, v) -> v }
                    .take(10)

                val paymentBreakdown = filteredExpenses
                    .groupBy { it.paymentMethod }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { (_, v) -> v }

                val monthlyTrend = DateUtils.getLast12Months().map { (m, y) ->
                    val monthStart = DateUtils.getStartOfMonth(m, y)
                    val monthEnd = DateUtils.getEndOfMonth(m, y)
                    val exp = expenses.filter { e ->
                        e.type == TransactionType.EXPENSE && e.currency == currency &&
                        e.date.time in monthStart.time..monthEnd.time
                    }.sumOf { it.amount }
                    val inc = incomes.filter { i ->
                        i.currency == currency && i.date.time in monthStart.time..monthEnd.time
                    }.sumOf { it.amount }
                    MonthlyData(m, y, exp, inc, inc - exp, currency)
                }

                val myrExpenses = expenses.filter { it.type == TransactionType.EXPENSE && it.currency == "MYR" }.sumOf { it.amount }
                val inrExpenses = expenses.filter { it.type == TransactionType.EXPENSE && it.currency == "INR" }.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedCurrency = currency,
                        selectedPeriod = period,
                        totalExpenses = totalExpenses,
                        totalIncome = totalIncome,
                        netSavings = totalIncome - totalExpenses,
                        categoryBreakdown = categoryBreakdown,
                        merchantBreakdown = merchantBreakdown,
                        monthlyTrend = monthlyTrend,
                        paymentBreakdown = paymentBreakdown,
                        myrExpenses = myrExpenses,
                        inrExpenses = inrExpenses,
                        transactionCount = filteredExpenses.size,
                        averageTransaction = if (filteredExpenses.isNotEmpty()) totalExpenses / filteredExpenses.size else 0.0
                    )
                }
            }.collect()
        }
    }

    fun setPeriod(period: AnalyticsPeriod) { _period.value = period }
    fun setCurrency(currency: String) {
        viewModelScope.launch { prefsDataStore.setDefaultCurrency(currency) }
    }

    private fun getPeriodDates(period: AnalyticsPeriod): Pair<java.util.Date, java.util.Date> {
        val cal = Calendar.getInstance()
        return when (period) {
            AnalyticsPeriod.THIS_MONTH -> Pair(
                DateUtils.getStartOfMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)),
                DateUtils.getEndOfMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
            )
            AnalyticsPeriod.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                Pair(
                    DateUtils.getStartOfMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)),
                    DateUtils.getEndOfMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
                )
            }
            AnalyticsPeriod.LAST_3_MONTHS -> Pair(DateUtils.monthsAgo(3), java.util.Date())
            AnalyticsPeriod.LAST_6_MONTHS -> Pair(DateUtils.monthsAgo(6), java.util.Date())
            AnalyticsPeriod.THIS_YEAR -> Pair(DateUtils.getStartOfYear(cal.get(Calendar.YEAR)), java.util.Date())
            AnalyticsPeriod.ALL_TIME -> Pair(java.util.Date(0), java.util.Date())
        }
    }
}
