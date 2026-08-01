package com.expensetracker.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.PaymentMethod
import com.expensetracker.ui.components.TransactionItem
import com.expensetracker.util.Format

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionListScreen(
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(uiState.isSearchActive) {
        if (uiState.isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    val hasActiveFilter = uiState.searchQuery.isNotBlank() ||
            uiState.filterType != null ||
            uiState.filterCategory != null ||
            uiState.filterPaymentMethod != null ||
            uiState.filterStartDate != null ||
            uiState.filterEndDate != null

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                AnimatedVisibility(
                    visible = uiState.isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search transactions...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                AnimatedVisibility(
                    visible = !uiState.isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text("Transactions", fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = {
                if (uiState.isSearchActive) {
                    IconButton(onClick = { viewModel.deactivateSearch() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                    }
                }
            },
            actions = {
                if (uiState.isSearchActive) {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                } else {
                    IconButton(onClick = { viewModel.activateSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    if (hasActiveFilter) {
                        IconButton(onClick = { viewModel.clearFilters() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear filters")
                        }
                    }
                }
            }
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Expense", "Income").forEach { type ->
                    FilterChip(
                        selected = uiState.filterType == type,
                        onClick = {
                            viewModel.setFilterType(
                                if (uiState.filterType == type) null else type
                            )
                        },
                        label = { Text(type) }
                    )
                }
            }

            Text("Payment Method", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isLoading) {
                            "Loading..."
                        } else if (hasActiveFilter) {
                            "No transactions match your search"
                        } else {
                            "No transactions found"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasActiveFilter && !uiState.isLoading) {
                        IconButton(onClick = { viewModel.clearFilters() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear all")
                        }
                        Text(
                            text = "Clear search & filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                        onClick = { onEditTransaction(transaction.id) },
                        onDelete = { transactionToDelete = transaction }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = {
                Text(
                    "Delete ${transaction.category} of ${Format.inr(transaction.amount)}? " +
                        "It will be removed from Google Sheets on the next sync."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transaction)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
