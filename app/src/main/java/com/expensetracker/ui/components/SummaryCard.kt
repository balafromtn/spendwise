package com.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.theme.ExpenseOnBrand
import com.expensetracker.ui.theme.IncomeOnBrand
import com.expensetracker.ui.theme.LocalBrandGradient
import com.expensetracker.util.Format

@Composable
fun SummaryCard(
    title: String,
    income: Double,
    expense: Double,
    savings: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(LocalBrandGradient.current, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(
                    icon = Icons.Default.ArrowUpward,
                    label = "Income",
                    value = Format.inr(income),
                    color = IncomeOnBrand
                )
                SummaryStat(
                    icon = Icons.Default.ArrowDownward,
                    label = "Expense",
                    value = Format.inr(expense),
                    color = ExpenseOnBrand
                )
                SummaryStat(
                    icon = Icons.Default.AccountBalance,
                    label = "Savings",
                    value = Format.inr(savings),
                    color = if (savings >= 0) IncomeOnBrand else ExpenseOnBrand
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(top = 2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
