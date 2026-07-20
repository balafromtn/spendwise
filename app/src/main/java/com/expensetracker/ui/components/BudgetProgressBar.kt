package com.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expensetracker.domain.model.Budget
import com.expensetracker.ui.theme.OverBudgetRed
import com.expensetracker.ui.theme.SafeGreen
import com.expensetracker.ui.theme.WarningYellow
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun BudgetProgressBar(
    budget: Budget,
    modifier: Modifier = Modifier
) {
    val progress = (budget.utilizationPercent / 100).coerceIn(0.0, 1.5).toFloat()
    val color = when {
        budget.isOverBudget -> OverBudgetRed
        budget.utilizationPercent >= 75.0 -> WarningYellow
        else -> SafeGreen
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = budget.category,
            style = MaterialTheme.typography.bodyMedium
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text(
            text = "\u20B9${"%.0f".format(budget.spentSoFar)} / \u20B9${"%.0f".format(budget.budgetAmount)} (${budget.utilizationPercent.toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = if (budget.isOverBudget) OverBudgetRed else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
