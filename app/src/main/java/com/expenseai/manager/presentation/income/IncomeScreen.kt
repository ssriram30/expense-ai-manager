package com.expenseai.manager.presentation.income

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.presentation.components.*
import com.expenseai.manager.ui.theme.IncomeGreenLight
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    onBack: () -> Unit,
    viewModel: IncomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ExpenseTopAppBar(title = "Income", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddSheet) { Icon(Icons.Default.Add, "Add") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Total Income Card
            Card(modifier = Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = IncomeGreenLight, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Total Income", style = MaterialTheme.typography.labelMedium)
                        Text(
                            CurrencyUtils.format(state.totalIncome, state.selectedCurrency),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreenLight
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.incomes.isEmpty()) {
                EmptyState("No income recorded yet", Icons.Default.TrendingUp)
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(state.incomes, key = { it.id }) { income ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { viewModel.editIncome(income) }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(income.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${income.source.emoji} ${income.source.displayName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(DateUtils.formatDisplay(income.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("+${CurrencyUtils.format(income.amount, income.currency)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IncomeGreenLight)
                                    IconButton(onClick = { viewModel.deleteIncome(income) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::hideAddSheet) {
            Column(modifier = Modifier.padding(16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (state.editingIncome != null) "Edit Income" else "Add Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = state.title, onValueChange = viewModel::setTitle,
                    label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountTextField(value = state.amount, onValueChange = viewModel::setAmount, currency = state.currency, modifier = Modifier.weight(2f))
                    CurrencyDropdown(selected = state.currency, onSelected = viewModel::setCurrency, modifier = Modifier.weight(1.5f))
                }

                CategoryDropdown(selected = state.source, onSelected = viewModel::setSource, categories = com.expenseai.manager.domain.model.ExpenseCategory.incomeCategories())

                DatePickerRow(date = state.date, onDateSelected = viewModel::setDate)

                OutlinedTextField(value = state.notes, onValueChange = viewModel::setNotes,
                    label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recurring Income", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.isRecurring, onCheckedChange = viewModel::setIsRecurring)
                }

                Button(onClick = viewModel::saveIncome, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                    Text("Save Income")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
