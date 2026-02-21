package com.studentbudget.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: BudgetViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val currentMonth = YearMonth.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // Group expenses by day of the current month
    val monthExpenses = viewModel.getMonthExpenses(allExpenses, currentMonth)
    val dailyTotals = mutableMapOf<Int, Double>()
    monthExpenses.forEach { exp ->
        try {
            val date = LocalDate.parse(exp.date)
            if (date.year == currentMonth.year && date.month == currentMonth.month) {
                dailyTotals[date.dayOfMonth] = (dailyTotals[date.dayOfMonth] ?: 0.0) + exp.amount
            }
        } catch (e: Exception) {}
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineLarge,
                color = White
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                Text(
                    text = "$monthLabel ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Build a simple 7-column grid for days
                val startOffset = currentMonth.atDay(1).dayOfWeek.value - 1 

                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = SecondaryGrey,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var currentDay = 1
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            if (r == 0 && c < startOffset) {
                                Box(modifier = Modifier.weight(1f).height(40.dp))
                            } else if (currentDay <= daysInMonth) {
                                val spent = dailyTotals[currentDay] ?: 0.0
                                val hasSpent = spent > 0
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(if (hasSpent) ElevatedGrey else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentDay.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (hasSpent) FontWeight.Bold else FontWeight.Normal),
                                            color = if (hasSpent) White else MidGrey
                                        )
                                        if (hasSpent) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(White))
                                        }
                                    }
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f).height(40.dp))
                            }
                        }
                    }
                }
            }
        }

        // Daily breakdown list
        if (dailyTotals.isNotEmpty()) {
            item {
                Text(
                    text = "DAILY SPENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            items(dailyTotals.entries.sortedByDescending { it.key }.toList()) { (day, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkGrey)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day $day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                    Text(
                        text = formatCurrency(amount, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = White
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(56.dp)) }
    }
}
