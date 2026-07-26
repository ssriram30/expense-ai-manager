package com.expenseai.manager.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddEditExpenseUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val amount: String = "",
    val currency: String = "MYR",
    val category: ExpenseCategory = ExpenseCategory.FOOD_DINING,
    val merchant: String = "",
    val date: Date = Date(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val tags: String = "",
    val receiptImagePath: String? = null,
    val isRecurring: Boolean = false,
    val taxAmount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val titleError: String? = null,
    val amountError: String? = null
)

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditExpenseUiState())
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.first().let { prefs ->
                _uiState.update { it.copy(currency = prefs.defaultCurrency) }
            }
        }
    }

    fun loadExpense(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            expenseRepo.getExpenseById(id)?.let { expense ->
                _uiState.update {
                    it.copy(
                        id = expense.id,
                        title = expense.title,
                        description = expense.description,
                        amount = expense.amount.toString(),
                        currency = expense.currency,
                        category = expense.category,
                        merchant = expense.merchant,
                        date = expense.date,
                        paymentMethod = expense.paymentMethod,
                        notes = expense.notes,
                        tags = expense.tags.joinToString(", "),
                        receiptImagePath = expense.receiptImagePath,
                        isRecurring = expense.isRecurring,
                        taxAmount = if (expense.taxAmount > 0) expense.taxAmount.toString() else "",
                        type = expense.type
                    )
                }
            }
        }
    }

    fun prefillFromOCR(
        title: String, amount: Double, currency: String, merchant: String,
        date: Date, taxAmount: Double, paymentMethod: PaymentMethod, category: ExpenseCategory
    ) {
        _uiState.update {
            it.copy(
                title = title.ifBlank { merchant },
                amount = if (amount > 0) amount.toString() else "",
                currency = currency,
                merchant = merchant,
                date = date,
                taxAmount = if (taxAmount > 0) taxAmount.toString() else "",
                paymentMethod = paymentMethod,
                category = category
            )
        }
    }

    fun setTitle(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun setDescription(v: String) = _uiState.update { it.copy(description = v) }
    fun setAmount(v: String) = _uiState.update { it.copy(amount = v, amountError = null) }
    fun setCurrency(v: String) = _uiState.update { it.copy(currency = v) }
    fun setCategory(v: ExpenseCategory) = _uiState.update { it.copy(category = v) }
    fun setMerchant(v: String) = _uiState.update { it.copy(merchant = v) }
    fun setDate(v: Date) = _uiState.update { it.copy(date = v) }
    fun setPaymentMethod(v: PaymentMethod) = _uiState.update { it.copy(paymentMethod = v) }
    fun setNotes(v: String) = _uiState.update { it.copy(notes = v) }
    fun setTags(v: String) = _uiState.update { it.copy(tags = v) }
    fun setReceiptPath(v: String?) = _uiState.update { it.copy(receiptImagePath = v) }
    fun setIsRecurring(v: Boolean) = _uiState.update { it.copy(isRecurring = v) }
    fun setTaxAmount(v: String) = _uiState.update { it.copy(taxAmount = v) }
    fun setType(v: TransactionType) = _uiState.update { it.copy(type = v) }

    fun save() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val expense = Expense(
                    id = state.id,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    amount = state.amount.toDouble(),
                    currency = state.currency,
                    category = state.category,
                    merchant = state.merchant.trim(),
                    date = state.date,
                    paymentMethod = state.paymentMethod,
                    notes = state.notes.trim(),
                    tags = state.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    receiptImagePath = state.receiptImagePath,
                    isRecurring = state.isRecurring,
                    taxAmount = state.taxAmount.toDoubleOrNull() ?: 0.0,
                    type = state.type
                )
                if (state.id == 0L) expenseRepo.insertExpense(expense)
                else expenseRepo.updateExpense(expense)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun validate(state: AddEditExpenseUiState): Boolean {
        var valid = true
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            valid = false
        }
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            valid = false
        }
        return valid
    }
}
