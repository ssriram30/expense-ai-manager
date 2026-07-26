package com.expenseai.manager.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_NOTIFICATION_BUDGET = booleanPreferencesKey("notification_budget")
        val KEY_NOTIFICATION_RECURRING = booleanPreferencesKey("notification_recurring")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_MONTHLY_BUDGET_MYR = doublePreferencesKey("monthly_budget_myr")
        val KEY_MONTHLY_BUDGET_INR = doublePreferencesKey("monthly_budget_inr")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_LAST_BACKUP = longPreferencesKey("last_backup")
        val KEY_EXCHANGE_RATE_MYR_INR = doublePreferencesKey("exchange_rate_myr_inr")
        val KEY_EXCHANGE_RATE_UPDATED = longPreferencesKey("exchange_rate_updated")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserPreferences(
                isDarkMode = prefs[KEY_DARK_MODE] ?: false,
                isDynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
                defaultCurrency = prefs[KEY_DEFAULT_CURRENCY] ?: "MYR",
                isBiometricEnabled = prefs[KEY_BIOMETRIC_ENABLED] ?: false,
                isPinEnabled = prefs[KEY_PIN_ENABLED] ?: false,
                pinHash = prefs[KEY_PIN_HASH] ?: "",
                notifyBudgetAlerts = prefs[KEY_NOTIFICATION_BUDGET] ?: true,
                notifyRecurring = prefs[KEY_NOTIFICATION_RECURRING] ?: true,
                language = prefs[KEY_LANGUAGE] ?: "en",
                monthlyBudgetMYR = prefs[KEY_MONTHLY_BUDGET_MYR] ?: 0.0,
                monthlyBudgetINR = prefs[KEY_MONTHLY_BUDGET_INR] ?: 0.0,
                onboardingDone = prefs[KEY_ONBOARDING_DONE] ?: false,
                lastBackupTime = prefs[KEY_LAST_BACKUP] ?: 0L,
                exchangeRateMyrToInr = prefs[KEY_EXCHANGE_RATE_MYR_INR] ?: 18.5,
                exchangeRateUpdatedAt = prefs[KEY_EXCHANGE_RATE_UPDATED] ?: 0L
            )
        }

    suspend fun setDarkMode(enabled: Boolean) =
        dataStore.edit { it[KEY_DARK_MODE] = enabled }

    suspend fun setDynamicColor(enabled: Boolean) =
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }

    suspend fun setDefaultCurrency(currency: String) =
        dataStore.edit { it[KEY_DEFAULT_CURRENCY] = currency }

    suspend fun setBiometricEnabled(enabled: Boolean) =
        dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }

    suspend fun setPinEnabled(enabled: Boolean, pinHash: String = "") {
        dataStore.edit {
            it[KEY_PIN_ENABLED] = enabled
            if (pinHash.isNotEmpty()) it[KEY_PIN_HASH] = pinHash
        }
    }

    suspend fun setNotifyBudget(enabled: Boolean) =
        dataStore.edit { it[KEY_NOTIFICATION_BUDGET] = enabled }

    suspend fun setNotifyRecurring(enabled: Boolean) =
        dataStore.edit { it[KEY_NOTIFICATION_RECURRING] = enabled }

    suspend fun setLanguage(lang: String) =
        dataStore.edit { it[KEY_LANGUAGE] = lang }

    suspend fun setMonthlyBudgetMYR(amount: Double) =
        dataStore.edit { it[KEY_MONTHLY_BUDGET_MYR] = amount }

    suspend fun setMonthlyBudgetINR(amount: Double) =
        dataStore.edit { it[KEY_MONTHLY_BUDGET_INR] = amount }

    suspend fun setOnboardingDone() =
        dataStore.edit { it[KEY_ONBOARDING_DONE] = true }

    suspend fun updateLastBackup() =
        dataStore.edit { it[KEY_LAST_BACKUP] = System.currentTimeMillis() }

    suspend fun updateExchangeRate(myrToInr: Double) {
        dataStore.edit {
            it[KEY_EXCHANGE_RATE_MYR_INR] = myrToInr
            it[KEY_EXCHANGE_RATE_UPDATED] = System.currentTimeMillis()
        }
    }
}

data class UserPreferences(
    val isDarkMode: Boolean,
    val isDynamicColor: Boolean,
    val defaultCurrency: String,
    val isBiometricEnabled: Boolean,
    val isPinEnabled: Boolean,
    val pinHash: String,
    val notifyBudgetAlerts: Boolean,
    val notifyRecurring: Boolean,
    val language: String,
    val monthlyBudgetMYR: Double,
    val monthlyBudgetINR: Double,
    val onboardingDone: Boolean,
    val lastBackupTime: Long,
    val exchangeRateMyrToInr: Double,
    val exchangeRateUpdatedAt: Long
)
