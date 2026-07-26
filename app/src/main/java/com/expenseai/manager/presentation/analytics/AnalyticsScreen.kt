package com.expenseai.manager.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.expenseai.manager.ui.theme.*
import com.expenseai.manager.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExpenseTopAppBar(
                title = "Analytics",
                onBack = onBack,
                actions = {
                    Box {
                        TextButton(onClick = { showPeriodMenu = true }) {
                            Text(state.selectedPeriod.label)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = showPeriodMenu, onDismissRequest = { showPeriodMenu = false }) {
                            AnalyticsPeriod.values().forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period.label) },
                                    onClick = { viewModel.setPeriod(period); showPeriodMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) { LoadingIndicator(Modifier.padding(padding)); return@Scaffold }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Total Spending",
                        value = CurrencyUtils.format(state.totalExpenses, state.selectedCurrency),
                        subtitle = "${state.transactionCount} transactions",
                        icon = Icons.Default.TrendingDown,
                        iconTint = ExpenseRedLight,
                        valueColor = ExpenseRedLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Income",
                        value = CurrencyUtils.format(state.totalIncome, state.selectedCurrency),
                        icon = Icons.Default.TrendingUp,
                        iconTint = IncomeGreenLight,
                        valueColor = IncomeGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Net Savings",
                        value = CurrencyUtils.format(state.netSavings, state.selectedCurrency),
                        icon = Icons.Default.Savings,
                        iconTint = if (state.netSavings >= 0) SavingsBlueLight else ExpenseRedLight,
                        valueColor = if (state.netSavings >= 0) SavingsBlueLight else ExpenseRedLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg. Transaction",
                        value = CurrencyUtils.format(state.averageTransaction, state.selectedCurrency),
                        icon = Icons.Default.Calculate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // MYR vs INR Comparison
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Currency Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🇲🇾 MYR", style = MaterialTheme.typography.labelSmall)
                                    Text(CurrencyUtils.format(state.myrExpenses, "MYR"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🇮🇳 INR", style = MaterialTheme.typography.labelSmall)
                                    Text(CurrencyUtils.format(state.inrExpenses, "INR"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Trend
            if (state.monthlyTrend.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("12-Month Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            val points = state.monthlyTrend.map { "${it.month}/${it.year.toString().takeLast(2)}" to it.expenses }
                            LineChart(points = points, modifier = Modifier.fillMaxWidth().height(160.dp), fillArea = true)
                            Spacer(Modifier.height(8.dp))
                            // Monthly bar chart
                            BarChart(
                                data = state.monthlyTrend.map { BarData("${it.month}/${it.year.toString().takeLast(2)}", it.expenses) },
                                modifier = Modifier.fillMaxWidth().height(120.dp)
                            )
                        }
                    }
                }
            }

            // Category Breakdown
            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Category Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            val total = state.categoryBreakdown.sumOf { (_, v) -> v }
                            val slices = state.categoryBreakdown.mapIndexed { i, (cat, amt) ->
                                PieSlice(cat.displayName, amt, chartColors[i % chartColors.size])
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedPieChart(slices = slices, modifier = Modifier.size(140.dp))
                                Spacer(Modifier.width(16.dp))
                                PieChartLegend(slices = slices, total = total, modifier = Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(12.dp))
                            state.categoryBreakdown.sortedByDescending { (_, v) -> v }.take(8).forEach { (cat, amt) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${cat.emoji} ${cat.displayName}", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyUtils.format(amt, state.selectedCurrency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Payment Methods
            if (state.paymentBreakdown.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Payment Methods", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            state.paymentBreakdown.forEach { (method, amt) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(method.displayName, style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyUtils.format(amt, state.selectedCurrency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Top Merchants
            if (state.merchantBreakdown.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Top Merchants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            state.merchantBreakdown.take(8).forEachIndexed { i, (merchant, amt) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${i + 1}.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                                        Text(merchant, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(CurrencyUtils.format(amt, state.selectedCurrency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
