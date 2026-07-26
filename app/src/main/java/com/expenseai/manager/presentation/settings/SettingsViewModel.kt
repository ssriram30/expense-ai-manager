package com.expenseai.manager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.data.datastore.UserPreferences
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.util.BackupManager
import com.expenseai.manager.util.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val backupManager: BackupManager
) : ViewModel() {

    val preferences = dataStore.userPreferences.stateIn(
        viewModelScope, SharingStarted.Eagerly,
        UserPreferences(false, true, "MYR", false, false, "", true, true, "en", 0.0, 0.0, false, 0L, 18.5, 0L)
    )

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { dataStore.setDarkMode(enabled) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { dataStore.setDynamicColor(enabled) }
    fun setDefaultCurrency(currency: String) = viewModelScope.launch { dataStore.setDefaultCurrency(currency) }
    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch { dataStore.setBiometricEnabled(enabled) }
    fun setPinEnabled(enabled: Boolean, pin: String = "") = viewModelScope.launch {
        val hash = if (enabled && pin.isNotBlank()) BiometricHelper.hashPin(pin) else ""
        dataStore.setPinEnabled(enabled, hash)
    }
    fun setNotifyBudget(enabled: Boolean) = viewModelScope.launch { dataStore.setNotifyBudget(enabled) }
    fun setNotifyRecurring(enabled: Boolean) = viewModelScope.launch { dataStore.setNotifyRecurring(enabled) }
    fun setMonthlyBudgetMYR(amount: Double) = viewModelScope.launch { dataStore.setMonthlyBudgetMYR(amount) }
    fun setMonthlyBudgetINR(amount: Double) = viewModelScope.launch { dataStore.setMonthlyBudgetINR(amount) }

    fun createBackup(): File = backupManager.createBackup()
    fun getAvailableBackups(): List<File> = backupManager.getAvailableBackups()
    fun restoreBackup(file: File): Boolean = backupManager.restoreBackup(file)
}
