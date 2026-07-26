package com.expenseai.manager.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.InsightData
import com.expenseai.manager.domain.repository.*
import com.expenseai.manager.util.AIInsightsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<InsightData> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCurrency: String = "MYR"
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val incomeRepo: IncomeRepository,
    private val budgetRepo: BudgetRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val insightsEngine: AIInsightsEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepo.getAllExpenses(),
                incomeRepo.getAllIncomes(),
                budgetRepo.getAllBudgets(),
                prefsDataStore.userPreferences
            ) { expenses, incomes, budgets, prefs ->
                val currency = prefs.defaultCurrency
                val insights = insightsEngine.generateInsights(expenses, incomes, budgets, currency)
                _uiState.update {
                    it.copy(insights = insights, isLoading = false, selectedCurrency = currency)
                }
            }.collect()
        }
    }
}
