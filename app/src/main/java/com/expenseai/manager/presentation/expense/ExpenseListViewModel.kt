package com.expenseai.manager.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCurrency: String = "MYR",
    val filterCategory: ExpenseCategory? = null,
    val filterType: TransactionType? = null,
    val sortBy: ExpenseSortOrder = ExpenseSortOrder.DATE_DESC
)

enum class ExpenseSortOrder(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _filterCategory = MutableStateFlow<ExpenseCategory?>(null)
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    private val _sortBy = MutableStateFlow(ExpenseSortOrder.DATE_DESC)

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepo.getAllExpenses(),
                prefsDataStore.userPreferences,
                _filterCategory,
                _filterType,
                _sortBy
            ) { expenses, prefs, category, type, sort ->
                val filtered = expenses
                    .filter { e -> category == null || e.category == category }
                    .filter { e -> type == null || e.type == type }
                    .let { list ->
                        when (sort) {
                            ExpenseSortOrder.DATE_DESC -> list.sortedByDescending { it.date }
                            ExpenseSortOrder.DATE_ASC -> list.sortedBy { it.date }
                            ExpenseSortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
                            ExpenseSortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
                        }
                    }

                _uiState.update {
                    it.copy(
                        expenses = expenses,
                        filteredExpenses = filtered,
                        isLoading = false,
                        selectedCurrency = prefs.defaultCurrency,
                        filterCategory = category,
                        filterType = type,
                        sortBy = sort
                    )
                }
            }.collect()
        }
    }

    fun setFilterCategory(category: ExpenseCategory?) { _filterCategory.value = category }
    fun setFilterType(type: TransactionType?) { _filterType.value = type }
    fun setSortBy(sort: ExpenseSortOrder) { _sortBy.value = sort }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseRepo.deleteExpense(expense) }
    }
}
