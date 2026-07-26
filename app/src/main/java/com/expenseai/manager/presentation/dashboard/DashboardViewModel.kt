package com.expenseai.manager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.data.remote.ExchangeRateApi
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.*
import com.expenseai.manager.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val selectedCurrency: String = "MYR",
    val currentMonth: Int = DateUtils.getCurrentMonth(),
    val currentYear: Int = DateUtils.getCurrentYear(),
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val categoryBreakdown: Map<ExpenseCategory, Double> = emptyMap(),
    val monthlyTrend: List<Pair<String, Double>> = emptyList(),
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val exchangeRateMyrToInr: Double = 18.5,
    val myrTotal: Double = 0.0,
    val inrTotal: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val incomeRepo: IncomeRepository,
    private val budgetRepo: BudgetRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val exchangeRateApi: ExchangeRateApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        fetchExchangeRate()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                expenseRepo.getAllExpenses(),
                incomeRepo.getAllIncomes(),
                budgetRepo.getAllBudgets(),
                prefsDataStore.userPreferences
            ) { expenses, incomes, budgets, prefs ->
                val currency = prefs.defaultCurrency
                val cal = Calendar.getInstance()
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val startOfMonth = DateUtils.getStartOfMonth(month, year)
                val endOfMonth = DateUtils.getEndOfMonth(month, year)

                val monthExpenses = expenses.filter {
                    it.type == TransactionType.EXPENSE &&
                    it.currency == currency &&
                    it.date in startOfMonth..endOfMonth
                }
                val monthIncome = incomes.filter {
                    it.currency == currency &&
                    it.date in startOfMonth..endOfMonth
                }

                val totalExpenses = monthExpenses.sumOf { it.amount }
                val totalIncome = monthIncome.sumOf { it.amount }
                val netSavings = totalIncome - totalExpenses

                val categoryBreakdown = monthExpenses
                    .groupBy { it.category }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }

                val monthlyTrend = DateUtils.getLast12Months().map { (m, y) ->
                    val monthStart = DateUtils.getStartOfMonth(m, y)
                    val monthEnd = DateUtils.getEndOfMonth(m, y)
                    val amount = expenses.filter {
                        it.type == TransactionType.EXPENSE && it.currency == currency &&
                        it.date in monthStart..monthEnd
                    }.sumOf { it.amount }
                    "${DateUtils.getMonthName(m)} ${y.toString().takeLast(2)}" to amount
                }

                val currentBudgets = budgets.filter { it.month == month && it.year == year && it.currency == currency }
                val budgetStatuses = currentBudgets.map { budget ->
                    val spent = monthExpenses
                        .filter { e -> budget.category == null || e.category == budget.category }
                        .sumOf { it.amount }
                    val remaining = budget.amount - spent
                    val pct = if (budget.amount > 0) spent / budget.amount else 0.0
                    BudgetStatus(budget, spent, remaining, pct, pct >= 1.0, pct >= budget.alertThreshold)
                }

                val myrTotal = expenses.filter { it.type == TransactionType.EXPENSE && it.currency == "MYR" }.sumOf { it.amount }
                val inrTotal = expenses.filter { it.type == TransactionType.EXPENSE && it.currency == "INR" }.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedCurrency = currency,
                        currentMonth = month,
                        currentYear = year,
                        totalExpenses = totalExpenses,
                        totalIncome = totalIncome,
                        netSavings = netSavings,
                        recentExpenses = expenses.sortedByDescending { e -> e.date }.take(10),
                        categoryBreakdown = categoryBreakdown,
                        monthlyTrend = monthlyTrend,
                        budgetStatuses = budgetStatuses,
                        myrTotal = myrTotal,
                        inrTotal = inrTotal,
                        exchangeRateMyrToInr = prefs.exchangeRateMyrToInr
                    )
                }
            }.collect()
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            prefsDataStore.setDefaultCurrency(currency)
        }
    }

    private fun fetchExchangeRate() {
        viewModelScope.launch {
            try {
                val response = exchangeRateApi.getLatestRates("MYR")
                val rate = response.rates["INR"] ?: 18.5
                prefsDataStore.updateExchangeRate(rate)
                _uiState.update { it.copy(exchangeRateMyrToInr = rate) }
            } catch (_: Exception) {}
        }
    }
}

private operator fun ClosedRange<Date>.contains(date: Date) =
    date.time >= start.time && date.time <= endInclusive.time
