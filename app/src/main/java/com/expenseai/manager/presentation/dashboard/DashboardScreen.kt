package com.expenseai.manager.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.presentation.components.*
import com.expenseai.manager.ui.theme.*
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToExpenseList: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currencies = listOf("MYR", "INR")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Expense AI Manager", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(DateUtils.getFullMonthName(state.currentMonth) + " ${state.currentYear}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, "Search") }
                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = onNavigateToScan, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(Icons.Default.CameraAlt, "Scan")
                }
                FloatingActionButton(onClick = onNavigateToAddExpense) {
                    Icon(Icons.Default.Add, "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Currency Tabs
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { currency ->
                        FilterChip(
                            selected = state.selectedCurrency == currency,
                            onClick = { viewModel.setCurrency(currency) },
                            label = {
                                Text("${CurrencyUtils.getCurrencyFlag(currency)} $currency")
                            }
                        )
                    }
                    FilterChip(
                        selected = state.selectedCurrency == "ALL",
                        onClick = { viewModel.setCurrency("MYR") },
                        label = { Text("🌐 All") }
                    )
                }
            }

            // Hero Summary Card
            item {
                GradientCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().height(200.dp),
                    startColor = Blue30,
                    endColor = Teal30
                ) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Total Spending", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(
                                    CurrencyUtils.format(state.totalExpenses, state.selectedCurrency),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MYR", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                                Text(CurrencyUtils.format(state.myrTotal, "MYR"), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text("INR", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                                Text(CurrencyUtils.format(state.inrTotal, "INR"), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Income", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyUtils.format(state.totalIncome, state.selectedCurrency),
                                    color = Color(0xFF81C784),
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Savings", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyUtils.format(state.netSavings, state.selectedCurrency),
                                    color = if (state.netSavings >= 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Rate: 1 MYR", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "= ${CurrencyUtils.format(state.exchangeRateMyrToInr, "INR")}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val actions = listOf(
                        Triple("Analytics", Icons.Default.BarChart, onNavigateToAnalytics),
                        Triple("Budget", Icons.Default.AccountBalance, onNavigateToBudget),
                        Triple("Income", Icons.Default.TrendingUp, onNavigateToIncome),
                        Triple("Transfers", Icons.Default.SwapHoriz, onNavigateToTransfers),
                        Triple("Insights", Icons.Default.Psychology, onNavigateToInsights),
                        Triple("All Expenses", Icons.Default.List, onNavigateToExpenseList)
                    )
                    items(actions) { (label, icon, action) ->
                        QuickActionCard(label = label, icon = icon, onClick = action)
                    }
                }
            }

            // Spending by Category
            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Spending by Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            val total = state.categoryBreakdown.values.sum()
                            val slices = state.categoryBreakdown.entries.take(8).mapIndexed { i, (cat, amt) ->
                                PieSlice(cat.displayName, amt, chartColors[i % chartColors.size])
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedPieChart(slices = slices, modifier = Modifier.size(130.dp))
                                Spacer(Modifier.width(16.dp))
                                PieChartLegend(slices = slices, total = total, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Monthly Trend
            if (state.monthlyTrend.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            LineChart(
                                points = state.monthlyTrend,
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                lineColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Budget Status
            if (state.budgetStatuses.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Budget Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            state.budgetStatuses.take(3).forEach { status ->
                                BudgetProgressBar(
                                    spent = status.spent,
                                    budget = status.budget.amount,
                                    currency = status.budget.currency,
                                    label = status.budget.name,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Expenses
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Expenses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onNavigateToExpenseList) { Text("See all") }
                }
            }

            if (state.recentExpenses.isEmpty()) {
                item { EmptyState("No expenses yet.\nTap + to add your first.", Icons.Default.Receipt) }
            } else {
                items(state.recentExpenses.take(5), key = { it.id }) { expense ->
                    ExpenseCard(expense = expense, onClick = {})
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 2)
        }
    }
}
