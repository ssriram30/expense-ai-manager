package com.expenseai.manager.presentation.budget

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

data class BudgetUiState(
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMonth: Int = DateUtils.getCurrentMonth(),
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val selectedCurrency: String = "MYR",
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val expenseRepo: ExpenseRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                budgetRepo.getAllBudgets(),
                expenseRepo.getAllExpenses(),
                prefsDataStore.userPreferences
            ) { budgets, expenses, prefs ->
                val state = _uiState.value
                val currency = prefs.defaultCurrency
                val month = state.selectedMonth
                val year = state.selectedYear
                val startOfMonth = DateUtils.getStartOfMonth(month, year)
                val endOfMonth = DateUtils.getEndOfMonth(month, year)

                val currentBudgets = budgets.filter {
                    it.month == month && it.year == year && it.currency == currency
                }

                val monthExpenses = expenses.filter { e ->
                    e.type == TransactionType.EXPENSE &&
                    e.currency == currency &&
                    e.date.time in startOfMonth.time..endOfMonth.time
                }

                val statuses = currentBudgets.map { budget ->
                    val spent = monthExpenses
                        .filter { e -> budget.category == null || e.category == budget.category }
                        .sumOf { it.amount }
                    val pct = if (budget.amount > 0) spent / budget.amount else 0.0
                    BudgetStatus(budget, spent, budget.amount - spent, pct, pct >= 1.0, pct >= budget.alertThreshold)
                }

                _uiState.update {
                    it.copy(
                        budgetStatuses = statuses,
                        isLoading = false,
                        selectedCurrency = currency,
                        totalBudget = currentBudgets.sumOf { it.amount },
                        totalSpent = monthExpenses.sumOf { it.amount }
                    )
                }
            }.collect()
        }
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch { budgetRepo.deleteBudget(budget) }

    fun navigateMonth(delta: Int) {
        val cal = Calendar.getInstance()
        cal.set(_uiState.value.selectedYear, _uiState.value.selectedMonth - 1, 1)
        cal.add(Calendar.MONTH, delta)
        _uiState.update {
            it.copy(
                selectedMonth = cal.get(Calendar.MONTH) + 1,
                selectedYear = cal.get(Calendar.YEAR)
            )
        }
    }
}
