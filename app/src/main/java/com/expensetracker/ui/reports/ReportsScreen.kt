package com.expensetracker.ui.reports

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.ui.components.PieChart
import com.expensetracker.ui.components.PieChartLegend
import com.expensetracker.ui.components.SummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val summary by viewModel.summary.collectAsState()

    val months = remember { DateUtils().availableMonths() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Monthly Report",
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
            SummaryCard(
                title = selectedMonth,
                income = summary.totalIncome,
                expense = summary.totalExpense,
                savings = summary.netSavings
            )
        }

        item {
            Text(
                text = "Transactions: ${summary.transactionCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val expenseBreakdown = summary.categoryBreakdown.filter { it.type == com.expensetracker.domain.model.TransactionType.EXPENSE }
        if (expenseBreakdown.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PieChart(data = expenseBreakdown, size = 180f)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PieChartLegend(data = expenseBreakdown)
                }
            }
        }

        val incomeBreakdown = summary.categoryBreakdown.filter { it.type == com.expensetracker.domain.model.TransactionType.INCOME }
        if (incomeBreakdown.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Income Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PieChartLegend(data = incomeBreakdown)
                }
            }
        }
    }
}
