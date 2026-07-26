package com.expenseai.manager.presentation.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.presentation.components.*

@Composable
fun AddBudgetScreen(
    budgetId: Long = -1,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddBudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isEditing = budgetId > 0

    LaunchedEffect(budgetId) { viewModel.loadBudget(budgetId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    Scaffold(
        topBar = { ExpenseTopAppBar(title = if (isEditing) "Edit Budget" else "Add Budget", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Budget Name *") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Label, null) },
                placeholder = { Text("e.g., Monthly Food Budget") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountTextField(
                    value = state.amount,
                    onValueChange = viewModel::setAmount,
                    currency = state.currency,
                    isError = state.amountError != null,
                    errorMessage = state.amountError ?: "",
                    modifier = Modifier.weight(2f)
                )
                CurrencyDropdown(selected = state.currency, onSelected = viewModel::setCurrency, modifier = Modifier.weight(1.5f))
            }

            // Category (optional)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = state.category?.let { "${it.emoji} ${it.displayName}" } ?: "All Categories",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category (optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("All Categories") }, onClick = { viewModel.setCategory(null); expanded = false })
                    ExpenseCategory.expenseCategories().forEach { cat ->
                        DropdownMenuItem(text = { Text("${cat.emoji} ${cat.displayName}") }, onClick = { viewModel.setCategory(cat); expanded = false })
                    }
                }
            }

            // Alert Threshold
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Alert Threshold", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("${(state.alertThreshold * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Text("Get notified when this % of budget is used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = state.alertThreshold,
                    onValueChange = viewModel::setAlertThreshold,
                    valueRange = 0.5f..0.95f,
                    steps = 8
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Update Budget" else "Create Budget")
                }
            }
        }
    }
}
