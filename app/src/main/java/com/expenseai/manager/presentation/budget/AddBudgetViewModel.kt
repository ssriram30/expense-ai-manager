package com.expenseai.manager.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.BudgetRepository
import com.expenseai.manager.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddBudgetUiState(
    val id: Long = 0,
    val name: String = "",
    val amount: String = "",
    val currency: String = "MYR",
    val category: ExpenseCategory? = null,
    val month: Int = DateUtils.getCurrentMonth(),
    val year: Int = DateUtils.getCurrentYear(),
    val alertThreshold: Float = 0.80f,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null
)

@HiltViewModel
class AddBudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBudgetUiState())
    val uiState: StateFlow<AddBudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.first().let { prefs ->
                _uiState.update { it.copy(currency = prefs.defaultCurrency) }
            }
        }
    }

    fun loadBudget(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            budgetRepo.getBudgetById(id)?.let { budget ->
                _uiState.update {
                    it.copy(
                        id = budget.id, name = budget.name, amount = budget.amount.toString(),
                        currency = budget.currency, category = budget.category,
                        month = budget.month, year = budget.year, alertThreshold = budget.alertThreshold.toFloat()
                    )
                }
            }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun setAmount(v: String) = _uiState.update { it.copy(amount = v, amountError = null) }
    fun setCurrency(v: String) = _uiState.update { it.copy(currency = v) }
    fun setCategory(v: ExpenseCategory?) = _uiState.update { it.copy(category = v) }
    fun setAlertThreshold(v: Float) = _uiState.update { it.copy(alertThreshold = v) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) { _uiState.update { it.copy(nameError = "Name required") }; return }
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) { _uiState.update { it.copy(amountError = "Valid amount required") }; return }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val budget = Budget(
                id = state.id, name = state.name.trim(), amount = amount,
                currency = state.currency, category = state.category,
                month = state.month, year = state.year, alertThreshold = state.alertThreshold.toDouble()
            )
            if (state.id == 0L) budgetRepo.insertBudget(budget) else budgetRepo.updateBudget(budget)
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
