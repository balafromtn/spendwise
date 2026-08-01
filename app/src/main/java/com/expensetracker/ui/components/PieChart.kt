package com.expensetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.domain.model.CategoryBreakdown
import com.expensetracker.ui.theme.ChartColors
import com.expensetracker.util.Format

@Composable
fun PieChart(
    data: List<CategoryBreakdown>,
    modifier: Modifier = Modifier,
    size: Float = 200f
) {
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "pie_animation"
    )

    LaunchedEffect(data) {
        animationProgress = 0f
        animationProgress = 1f
    }

    if (data.isEmpty()) {
        Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val total = data.sumOf { it.amount }
    if (total <= 0) {
        Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Canvas(modifier = modifier.size(size.dp)) {
        val strokeWidth = 35f
        val radius = (this.size.minDimension - strokeWidth) / 2
        val center = Offset(this.size.width / 2, this.size.height / 2)

        var startAngle = -90f
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.amount / total * 360f * animatedProgress).toFloat()
            val color = ChartColors[index % ChartColors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun PieChartLegend(
    data: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.amount }
    Column(modifier = modifier.fillMaxWidth()) {
        data.forEachIndexed { index, item ->
            val color = ChartColors[index % ChartColors.size]
            val percentage = if (total > 0) (item.amount / total * 100) else 0.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = color)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Format.inr(item.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${"%.1f".format(percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
