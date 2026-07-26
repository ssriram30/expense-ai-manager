package com.expenseai.manager.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.domain.model.Transfer
import com.expenseai.manager.domain.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class TransferUiState(
    val transfers: List<Transfer> = emptyList(),
    val isLoading: Boolean = true,
    val totalSentMYR: Double = 0.0,
    val totalReceivedINR: Double = 0.0,
    val showAddSheet: Boolean = false,
    // form
    val amount: String = "",
    val fromCurrency: String = "MYR",
    val toCurrency: String = "INR",
    val exchangeRate: String = "18.5",
    val fee: String = "0",
    val recipient: String = "",
    val transferMethod: String = "Bank Transfer",
    val date: Date = Date(),
    val notes: String = ""
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepo: TransferRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transferRepo.getAllTransfers(),
                prefsDataStore.userPreferences
            ) { transfers, prefs ->
                _uiState.update {
                    it.copy(
                        transfers = transfers,
                        isLoading = false,
                        totalSentMYR = transfers.filter { t -> t.fromCurrency == "MYR" }.sumOf { t -> t.amount },
                        totalReceivedINR = transfers.filter { t -> t.toCurrency == "INR" }.sumOf { t -> t.convertedAmount },
                        exchangeRate = prefs.exchangeRateMyrToInr.toString()
                    )
                }
            }.collect()
        }
    }

    fun showAddSheet() = _uiState.update { it.copy(showAddSheet = true) }
    fun hideAddSheet() = _uiState.update { it.copy(showAddSheet = false) }

    fun setAmount(v: String) = _uiState.update { it.copy(amount = v) }
    fun setFromCurrency(v: String) = _uiState.update { it.copy(fromCurrency = v) }
    fun setToCurrency(v: String) = _uiState.update { it.copy(toCurrency = v) }
    fun setExchangeRate(v: String) = _uiState.update { it.copy(exchangeRate = v) }
    fun setFee(v: String) = _uiState.update { it.copy(fee = v) }
    fun setRecipient(v: String) = _uiState.update { it.copy(recipient = v) }
    fun setTransferMethod(v: String) = _uiState.update { it.copy(transferMethod = v) }
    fun setDate(v: Date) = _uiState.update { it.copy(date = v) }
    fun setNotes(v: String) = _uiState.update { it.copy(notes = v) }

    fun saveTransfer() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val rate = state.exchangeRate.toDoubleOrNull() ?: 18.5
        val fee = state.fee.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val transfer = Transfer(
                amount = amount,
                fromCurrency = state.fromCurrency,
                toCurrency = state.toCurrency,
                exchangeRate = rate,
                convertedAmount = amount * rate,
                fee = fee,
                date = state.date,
                notes = state.notes.trim(),
                recipient = state.recipient.trim(),
                transferMethod = state.transferMethod
            )
            transferRepo.insertTransfer(transfer)
            hideAddSheet()
        }
    }

    fun deleteTransfer(transfer: Transfer) = viewModelScope.launch { transferRepo.deleteTransfer(transfer) }
}
