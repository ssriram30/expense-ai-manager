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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.repository.ExpenseRepository
import com.expenseai.manager.presentation.components.CategoryIcon
import com.expenseai.manager.presentation.components.ExpenseTopAppBar
import com.expenseai.manager.presentation.components.LoadingIndicator
import com.expenseai.manager.ui.theme.ExpenseRedLight
import com.expenseai.manager.ui.theme.IncomeGreenLight
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository
) : ViewModel() {
    private val _expense = MutableStateFlow<Expense?>(null)
    val expense: StateFlow<Expense?> = _expense.asStateFlow()

    fun loadExpense(id: Long) = viewModelScope.launch {
        _expense.value = expenseRepo.getExpenseById(id)
    }

    fun deleteExpense(expense: Expense, onDeleted: () -> Unit) = viewModelScope.launch {
        expenseRepo.deleteExpense(expense)
        onDeleted()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val expense by viewModel.expense.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) { viewModel.loadExpense(expenseId) }

    Scaffold(
        topBar = {
            ExpenseTopAppBar(
                title = "Transaction Detail",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        expense?.let { exp ->
            Column(
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CategoryIcon(exp.category, size = 64.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(exp.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (exp.merchant.isNotBlank()) {
                            Text(exp.merchant, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            CurrencyUtils.format(exp.amount, exp.currency),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRedLight
                        )
                        Text(DateUtils.formatFull(exp.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Details
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow("Category", "${exp.category.emoji} ${exp.category.displayName}")
                        HorizontalDivider()
                        DetailRow("Payment Method", exp.paymentMethod.displayName)
                        HorizontalDivider()
                        DetailRow("Currency", "${CurrencyUtils.getCurrencyFlag(exp.currency)} ${exp.currency}")
                        if (exp.taxAmount > 0) {
                            HorizontalDivider()
                            DetailRow("Tax / GST", CurrencyUtils.format(exp.taxAmount, exp.currency))
                        }
                        if (exp.description.isNotBlank()) {
                            HorizontalDivider()
                            DetailRow("Description", exp.description)
                        }
                        if (exp.notes.isNotBlank()) {
                            HorizontalDivider()
                            DetailRow("Notes", exp.notes)
                        }
                        if (exp.tags.isNotEmpty()) {
                            HorizontalDivider()
                            Row {
                                Text("Tags", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    exp.tags.forEach { tag ->
                                        AssistChip(onClick = {}, label = { Text(tag, style = MaterialTheme.typography.labelSmall) })
                                    }
                                }
                            }
                        }
                        if (exp.isRecurring) {
                            HorizontalDivider()
                            DetailRow("Recurring", "✅ Yes")
                        }
                    }
                }
            }
        } ?: LoadingIndicator()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        expense?.let { viewModel.deleteExpense(it) { onBack() } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
