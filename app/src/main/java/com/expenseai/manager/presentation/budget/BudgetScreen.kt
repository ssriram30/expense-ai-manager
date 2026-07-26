package com.expenseai.manager.presentation.budget

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
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    onAddBudget: () -> Unit,
    onBudgetClick: (Long) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ExpenseTopAppBar(
                title = "Budget Planner",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.navigateMonth(-1) }) { Icon(Icons.Default.ChevronLeft, "Prev") }
                    Text(
                        "${DateUtils.getFullMonthName(state.selectedMonth)} ${state.selectedYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { viewModel.navigateMonth(1) }) { Icon(Icons.Default.ChevronRight, "Next") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudget) {
                Icon(Icons.Default.Add, "Add Budget")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary
            Card(modifier = Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${DateUtils.getFullMonthName(state.selectedMonth)} Budget Overview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    BudgetProgressBar(
                        spent = state.totalSpent,
                        budget = state.totalBudget,
                        currency = state.selectedCurrency,
                        label = "Total Budget"
                    )
                }
            }

            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.budgetStatuses.isEmpty()) {
                EmptyState(
                    "No budgets set\nTap + to create your first budget",
                    Icons.Default.AccountBalance,
                    action = { Button(onClick = onAddBudget) { Text("Create Budget") } }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.budgetStatuses, key = { it.budget.id }) { status ->
                        BudgetCard(
                            status = status,
                            onClick = { onBudgetClick(status.budget.id) },
                            onDelete = { viewModel.deleteBudget(status.budget) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    status: com.expenseai.manager.domain.model.BudgetStatus,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor = when {
        status.isOverBudget -> MaterialTheme.colorScheme.errorContainer
        status.isNearLimit -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(status.budget.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    status.budget.category?.let { cat ->
                        Text("${cat.emoji} ${cat.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
                    if (status.isOverBudget) Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            BudgetProgressBar(
                spent = status.spent,
                budget = status.budget.amount,
                currency = status.budget.currency,
                label = if (status.isOverBudget) "⚠️ Over budget by ${CurrencyUtils.format(status.spent - status.budget.amount, status.budget.currency)}" else "Remaining: ${CurrencyUtils.format(status.remaining, status.budget.currency)}"
            )
        }
    }
}
