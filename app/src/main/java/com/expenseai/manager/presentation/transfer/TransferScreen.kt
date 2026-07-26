package com.expenseai.manager.presentation.transfer

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
fun TransferScreen(
    onBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ExpenseTopAppBar(title = "Money Transfers", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddSheet) {
                Icon(Icons.Default.SwapHoriz, "Add Transfer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary Cards
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🇲🇾 Total Sent", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyUtils.format(state.totalSentMYR, "MYR"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🇮🇳 Total Received", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyUtils.format(state.totalReceivedINR, "INR"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.transfers.isEmpty()) {
                EmptyState("No transfers yet", Icons.Default.SwapHoriz)
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.transfers, key = { it.id }) { transfer ->
                        Card(shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(transfer.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (transfer.recipient.isNotBlank()) Text("To: ${transfer.recipient}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(DateUtils.formatDisplay(transfer.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(CurrencyUtils.format(transfer.amount, transfer.fromCurrency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("→ ${CurrencyUtils.format(transfer.convertedAmount, transfer.toCurrency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        IconButton(onClick = { viewModel.deleteTransfer(transfer) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                if (transfer.fee > 0) {
                                    Text("Fee: ${CurrencyUtils.format(transfer.fee, transfer.fromCurrency)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Rate: 1 ${transfer.fromCurrency} = ${transfer.exchangeRate} ${transfer.toCurrency}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("New Transfer (MYR → INR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountTextField(value = state.amount, onValueChange = viewModel::setAmount, currency = state.fromCurrency, label = "Amount Sent", modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = state.exchangeRate, onValueChange = viewModel::setExchangeRate,
                    label = { Text("Exchange Rate (1 MYR = ? INR)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                // Show converted amount
                val amt = state.amount.toDoubleOrNull() ?: 0.0
                val rate = state.exchangeRate.toDoubleOrNull() ?: 18.5
                if (amt > 0) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text("Recipient gets: ${CurrencyUtils.format(amt * rate, "INR")}", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(value = state.fee, onValueChange = viewModel::setFee,
                    label = { Text("Transfer Fee (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(value = state.recipient, onValueChange = viewModel::setRecipient,
                    label = { Text("Recipient Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(value = state.notes, onValueChange = viewModel::setNotes,
                    label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                DatePickerRow(date = state.date, onDateSelected = viewModel::setDate)

                Button(onClick = viewModel::saveTransfer, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.SwapHoriz, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Record Transfer")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
