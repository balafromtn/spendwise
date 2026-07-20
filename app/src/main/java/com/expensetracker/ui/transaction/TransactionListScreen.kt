package com.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.domain.model.PaymentMethod
import com.expensetracker.ui.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionListScreen(
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Transactions", fontWeight = FontWeight.Bold) },
            actions = {
                if (uiState.filterCategory != null || uiState.filterPaymentMethod != null) {
                    IconButton(onClick = { viewModel.clearFilters() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear filters")
                    }
                }
            }
        )

        // Filter chips
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Payment Method", style = MaterialTheme.typography.labelSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = uiState.filterPaymentMethod == method.label,
                        onClick = {
                            viewModel.setFilterPaymentMethod(
                                if (uiState.filterPaymentMethod == method.label) null else method.label
                            )
                        },
                        label = { Text(method.label) }
                    )
                }
            }
        }

        if (uiState.filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (uiState.isLoading) "Loading..." else "No transactions found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.filteredTransactions) { transaction ->
                    TransactionItem(
                        category = transaction.category,
                        amount = transaction.amount,
                        type = transaction.type,
                        paymentMethod = transaction.paymentMethod,
                        time = transaction.time,
                        notes = transaction.notes,
                        modifier = Modifier.let { mod ->
                            mod
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
