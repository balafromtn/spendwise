package com.expensetracker.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.ui.components.BudgetProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf("") }
    var budgetAmountText by remember { mutableStateOf("") }

    val months = listOf(
        "Jan 2026", "Feb 2026", "Mar 2026", "Apr 2026",
        "May 2026", "Jun 2026", "Jul 2026", "Aug 2026",
        "Sep 2026", "Oct 2026", "Nov 2026", "Dec 2026"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Budget Tracking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(months) { month ->
                    FilterChip(
                        selected = selectedMonth == month,
                        onClick = { viewModel.selectMonth(month) },
                        label = { Text(month) }
                    )
                }
            }
        }

        item {
            TextButton(onClick = {
                editingCategory = ""
                budgetAmountText = ""
                showSetBudgetDialog = true
            }) {
                Text("+ Set Budget for Category")
            }
        }

        if (budgets.isEmpty()) {
            item {
                Text(
                    text = "No budgets set for this month. Tap above to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(budgets) { budget ->
                Column {
                    BudgetProgressBar(budget = budget)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showSetBudgetDialog) {
        val expenseCategories = listOf(
            "Food", "Transport", "Shopping", "Bills",
            "Entertainment", "Healthcare", "Education"
        )
        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { Text("Set Budget") },
            text = {
                Column {
                    Text("Category")
                    expenseCategories.forEach { cat ->
                        FilterChip(
                            selected = editingCategory == cat,
                            onClick = { editingCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = budgetAmountText,
                        onValueChange = { budgetAmountText = it },
                        label = { Text("Budget Amount (\u20B9)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = budgetAmountText.toDoubleOrNull()
                        if (editingCategory.isNotBlank() && amount != null && amount > 0) {
                            viewModel.setBudget(editingCategory, amount)
                            showSetBudgetDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
