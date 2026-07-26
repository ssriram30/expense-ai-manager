package com.expenseai.manager.presentation.expense

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
import com.expenseai.manager.domain.model.*
import com.expenseai.manager.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: Long = -1,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onScanReceipt: () -> Unit,
    viewModel: AddEditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isEditing = expenseId > 0

    LaunchedEffect(expenseId) { viewModel.loadExpense(expenseId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    Scaffold(
        topBar = {
            ExpenseTopAppBar(
                title = if (isEditing) "Edit Expense" else "Add Expense",
                onBack = onBack,
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = onScanReceipt) {
                            Icon(Icons.Default.CameraAlt, "Scan Receipt")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.values().forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { viewModel.setType(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title *") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.ShoppingBag, null) }
            )

            // Amount + Currency in a row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountTextField(
                    value = state.amount,
                    onValueChange = viewModel::setAmount,
                    currency = state.currency,
                    isError = state.amountError != null,
                    errorMessage = state.amountError ?: "",
                    modifier = Modifier.weight(2f)
                )
                CurrencyDropdown(
                    selected = state.currency,
                    onSelected = viewModel::setCurrency,
                    modifier = Modifier.weight(1.5f)
                )
            }

            // Category
            CategoryDropdown(
                selected = state.category,
                onSelected = viewModel::setCategory,
                categories = if (state.type == TransactionType.INCOME)
                    ExpenseCategory.incomeCategories() else ExpenseCategory.expenseCategories()
            )

            // Merchant
            OutlinedTextField(
                value = state.merchant,
                onValueChange = viewModel::setMerchant,
                label = { Text("Merchant / Store") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Store, null) }
            )

            // Date
            DatePickerRow(date = state.date, onDateSelected = viewModel::setDate)

            // Payment Method
            PaymentMethodDropdown(selected = state.paymentMethod, onSelected = viewModel::setPaymentMethod)

            // Tax (optional)
            OutlinedTextField(
                value = state.taxAmount,
                onValueChange = viewModel::setTaxAmount,
                label = { Text("Tax / GST (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Receipt, null) }
            )

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::setNotes,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Note, null) }
            )

            // Tags
            OutlinedTextField(
                value = state.tags,
                onValueChange = viewModel::setTags,
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Tag, null) },
                placeholder = { Text("e.g., work, lunch, reimbursable") }
            )

            // Recurring toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Recurring", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("Auto-track this expense monthly", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.isRecurring, onCheckedChange = viewModel::setIsRecurring)
            }

            Spacer(Modifier.height(8.dp))

            // Save button
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
                    Text(if (isEditing) "Update" else "Save Expense", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
