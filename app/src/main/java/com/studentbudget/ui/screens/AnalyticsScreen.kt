package com.studentbudget.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.ChartColors
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel

@Composable
fun AnalyticsScreen(viewModel: BudgetViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val analyticsMonth by viewModel.analyticsMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val monthExpenses = viewModel.getMonthExpenses(allExpenses, analyticsMonth)
    val expensesOnly = monthExpenses.filter { it.type == "expense" }
    val categoryTotals = viewModel.getCategoryTotals(expensesOnly)
    val totalSpent = expensesOnly.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with month picker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = White
                )
                MonthPicker(
                    label = viewModel.formatMonthLabel(analyticsMonth),
                    onPrev = { viewModel.shiftAnalyticsMonth(-1) },
                    onNext = { viewModel.shiftAnalyticsMonth(1) }
                )
            }
        }

        // Donut Chart
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                Text(
                    text = "SPENDING DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (categoryTotals.isEmpty()) {
                    Text(
                        text = "No data for this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MidGrey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            data = categoryTotals,
                            total = totalSpent,
                            symbol = currencySymbol,
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        ChartLegend(data = categoryTotals, total = totalSpent, viewModel = viewModel)
                    }
                }
            }
        }

        // Bar Chart
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                Text(
                    text = "BY CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (categoryTotals.isEmpty()) {
                    Text(
                        text = "No data for this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MidGrey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val maxAmount = categoryTotals.values.maxOrNull() ?: 1.0
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        categoryTotals.entries.forEachIndexed { index, (cat, amount) ->
                            BarChartRow(
                                icon = viewModel.resolveCategoryIcon(cat),
                                label = viewModel.resolveCategoryLabel(cat),
                                amount = amount,
                                symbol = currencySymbol,
                                fraction = (amount / maxAmount).toFloat(),
                                color = ChartColors[index % ChartColors.size]
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }
}

@Composable
fun MonthPicker(label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ChevronLeft, "Previous", tint = SecondaryGrey)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = SecondaryGrey
        )
        IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ChevronRight, "Next", tint = SecondaryGrey)
        }
    }
}

@Composable
fun DonutChart(data: Map<String, Double>, total: Double, symbol: String, modifier: Modifier = Modifier) {
    val formattedTotal = formatCurrency(total, symbol)

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val strokeWidth = radius * 0.35f

        var startAngle = -90f
        data.entries.forEachIndexed { index, (_, amount) ->
            val sweep = (amount / total * 360).toFloat()
            drawArc(
                color = ChartColors[index % ChartColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }

        // Center text
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = radius * 0.28f
                isFakeBoldText = true
                isAntiAlias = true
            }
            drawText(formattedTotal, center.x, center.y + paint.textSize / 3, paint)
        }
    }
}

@Composable
fun ChartLegend(data: Map<String, Double>, total: Double, viewModel: BudgetViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.entries.forEachIndexed { index, (cat, amount) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ChartColors[index % ChartColors.size])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${viewModel.resolveCategoryIcon(cat)} ${viewModel.resolveCategoryLabel(cat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryGrey
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${(amount / total * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
            }
        }
    }
}

@Composable
fun BarChartRow(icon: String, label: String, amount: Double, symbol: String, fraction: Float, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.width(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SecondaryGrey
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ElevatedGrey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatCurrency(amount, symbol),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = White,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}
