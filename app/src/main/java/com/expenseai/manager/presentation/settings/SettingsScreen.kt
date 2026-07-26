package com.expenseai.manager.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.presentation.components.*
import com.expenseai.manager.util.CurrencyUtils

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = { ExpenseTopAppBar(title = "Settings", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection(title = "Appearance") {
                SettingsSwitch("Dark Mode", "Use dark theme", Icons.Default.DarkMode, prefs.isDarkMode, viewModel::setDarkMode)
                HorizontalDivider()
                SettingsSwitch("Material You", "Use dynamic colors from wallpaper", Icons.Default.Palette, prefs.isDynamicColor, viewModel::setDynamicColor)
            }

            SettingsSection(title = "Currency") {
                var currencyExpanded by remember { mutableStateOf(false) }
                SettingsItem(
                    "Default Currency",
                    "${CurrencyUtils.getCurrencyFlag(prefs.defaultCurrency)} ${prefs.defaultCurrency} - ${CurrencyUtils.getCurrencyName(prefs.defaultCurrency)}",
                    Icons.Default.CurrencyExchange,
                    { currencyExpanded = !currencyExpanded }
                )
                if (currencyExpanded) {
                    Column {
                        CurrencyUtils.SUPPORTED_CURRENCIES.forEach { currency ->
                            ListItem(
                                headlineContent = { Text("${CurrencyUtils.getCurrencyFlag(currency)} $currency") },
                                supportingContent = { Text(CurrencyUtils.getCurrencyName(currency), style = MaterialTheme.typography.bodySmall) },
                                trailingContent = {
                                    if (currency == prefs.defaultCurrency) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier.clickable { viewModel.setDefaultCurrency(currency); currencyExpanded = false }
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "Security") {
                SettingsSwitch("Biometric Lock", "Use fingerprint/face to unlock", Icons.Default.Fingerprint, prefs.isBiometricEnabled, viewModel::setBiometricEnabled)
                HorizontalDivider()
                SettingsSwitch(
                    "PIN Lock", "Set a 4-6 digit PIN", Icons.Default.Pin, prefs.isPinEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) showPinDialog = true
                        else viewModel.setPinEnabled(false)
                    }
                )
            }

            SettingsSection(title = "Notifications") {
                SettingsSwitch("Budget Alerts", "Notify when approaching budget limit", Icons.Default.NotificationsActive, prefs.notifyBudgetAlerts, viewModel::setNotifyBudget)
                HorizontalDivider()
                SettingsSwitch("Recurring Reminders", "Remind for upcoming recurring expenses", Icons.Default.Repeat, prefs.notifyRecurring, viewModel::setNotifyRecurring)
            }

            SettingsSection(title = "Backup & Restore") {
                SettingsItem("Create Backup", "Save database to local storage", Icons.Default.Backup) {
                    try { viewModel.createBackup(); snackbarMessage = "Backup created successfully" }
                    catch (e: Exception) { snackbarMessage = "Backup failed: ${e.message}" }
                }
                val backups = viewModel.getAvailableBackups()
                if (backups.isNotEmpty()) {
                    HorizontalDivider()
                    SettingsItem("Restore Backup", "${backups.size} backup(s) available", Icons.Default.Restore) {
                        snackbarMessage = if (viewModel.restoreBackup(backups.first()))
                            "Backup restored. Please restart the app." else "Restore failed"
                    }
                }
            }

            SettingsSection(title = "About") {
                SettingsItem("Expense AI Manager", "Version 1.0.0 • Your Smart Finance Companion", Icons.Default.Info) {}
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false; pinInput = "" },
            title = { Text("Set PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                    label = { Text("4-6 digit PIN") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinInput.length >= 4) { viewModel.setPinEnabled(true, pinInput); showPinDialog = false; pinInput = ""; snackbarMessage = "PIN set" }
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false; pinInput = "" }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
private fun SettingsSwitch(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

@Composable
private fun SettingsItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
