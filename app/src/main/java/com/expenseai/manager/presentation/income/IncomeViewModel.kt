package com.expenseai.manager.presentation.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class IncomeUiState(
    val incomes: List<Income> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCurrency: String = "MYR",
    val totalIncome: Double = 0.0,
    val showAddSheet: Boolean = false,
    val editingIncome: Income? = null,
    // form
    val title: String = "",
    val amount: String = "",
    val currency: String = "MYR",
    val source: ExpenseCategory = ExpenseCategory.SALARY,
    val date: Date = Date(),
    val notes: String = "",
    val isRecurring: Boolean = false
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepo: IncomeRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                incomeRepo.getAllIncomes(),
                prefsDataStore.userPreferences
            ) { incomes, prefs ->
                val currency = prefs.defaultCurrency
                _uiState.update {
                    it.copy(
                        incomes = incomes.filter { i -> i.currency == currency },
                        isLoading = false,
                        selectedCurrency = currency,
                        totalIncome = incomes.filter { i -> i.currency == currency }.sumOf { i -> i.amount },
                        currency = currency
                    )
                }
            }.collect()
        }
    }

    fun showAddSheet() = _uiState.update { it.copy(showAddSheet = true, editingIncome = null, title = "", amount = "", notes = "") }
    fun hideAddSheet() = _uiState.update { it.copy(showAddSheet = false, editingIncome = null) }

    fun editIncome(income: Income) = _uiState.update {
        it.copy(
            showAddSheet = true, editingIncome = income,
            title = income.title, amount = income.amount.toString(),
            currency = income.currency, source = income.source,
            date = income.date, notes = income.notes, isRecurring = income.isRecurring
        )
    }

    fun setTitle(v: String) = _uiState.update { it.copy(title = v) }
    fun setAmount(v: String) = _uiState.update { it.copy(amount = v) }
    fun setCurrency(v: String) = _uiState.update { it.copy(currency = v) }
    fun setSource(v: ExpenseCategory) = _uiState.update { it.copy(source = v) }
    fun setDate(v: Date) = _uiState.update { it.copy(date = v) }
    fun setNotes(v: String) = _uiState.update { it.copy(notes = v) }
    fun setIsRecurring(v: Boolean) = _uiState.update { it.copy(isRecurring = v) }

    fun saveIncome() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val income = Income(
                id = state.editingIncome?.id ?: 0,
                title = state.title.trim(), amount = amount,
                currency = state.currency, source = state.source,
                date = state.date, notes = state.notes.trim(), isRecurring = state.isRecurring
            )
            if (income.id == 0L) incomeRepo.insertIncome(income) else incomeRepo.updateIncome(income)
            hideAddSheet()
        }
    }

    fun deleteIncome(income: Income) = viewModelScope.launch { incomeRepo.deleteIncome(income) }
}
