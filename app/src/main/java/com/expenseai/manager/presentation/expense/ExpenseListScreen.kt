package com.expenseai.manager.presentation.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.TransactionType
import com.expenseai.manager.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onScanReceipt: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExpenseTopAppBar(
                title = "All Transactions",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            ExpenseSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = { viewModel.setSortBy(order); showSortMenu = false },
                                    leadingIcon = {
                                        if (state.sortBy == order) Icon(Icons.Default.Check, null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(onClick = onScanReceipt, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(Icons.Default.CameraAlt, "Scan")
                }
                FloatingActionButton(onClick = onAddExpense) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Active filters chip row
            if (state.filterCategory != null || state.filterType != null) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.filterCategory?.let { cat ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setFilterCategory(null) },
                            label = { Text(cat.displayName) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    state.filterType?.let { type ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setFilterType(null) },
                            label = { Text(type.name) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.filteredExpenses.isEmpty()) {
                EmptyState(
                    "No transactions found",
                    Icons.Default.SearchOff,
                    action = {
                        Button(onClick = onAddExpense) { Text("Add Expense") }
                    }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(state.filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onClick = { onExpenseClick(expense.id) },
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategory = state.filterCategory,
            selectedType = state.filterType,
            onCategorySelected = { viewModel.setFilterCategory(it) },
            onTypeSelected = { viewModel.setFilterType(it) },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedCategory: ExpenseCategory?,
    selectedType: TransactionType?,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onTypeSelected: (TransactionType?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Text("Filter Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Text("Type", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                TransactionType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(if (selectedType == type) null else type) },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                ExpenseCategory.expenseCategories().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelected(if (selectedCategory == cat) null else cat) },
                        label = { Text("${cat.emoji} ${cat.displayName}") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Apply") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
