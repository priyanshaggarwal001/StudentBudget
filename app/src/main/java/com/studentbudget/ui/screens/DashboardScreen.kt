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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    onSeeAllHistory: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val smartInsights by viewModel.smartInsights.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    val currentMonthExpenses = remember(allExpenses) { viewModel.getCurrentMonthExpenses(allExpenses) }
    val totalSpent = remember(currentMonthExpenses) { currentMonthExpenses.sumOf { it.amount } }
    val categoryTotals = remember(currentMonthExpenses) { viewModel.getCategoryTotals(currentMonthExpenses) }
    val avgDaily = remember(currentMonthExpenses) { viewModel.getAverageDailySpending(currentMonthExpenses) }
    val recentExpenses = remember(allExpenses) { allExpenses.take(5) }

    // Days left in month
    val daysLeftInMonth = remember {
        val today = LocalDate.now()
        today.lengthOfMonth() - today.dayOfMonth
    }

    // Streak: consecutive days with spending under daily budget avg
    val streakDays = remember(allExpenses, monthlyBudget) {
        if (monthlyBudget <= 0) 0
        else {
            val dailyTarget = monthlyBudget / LocalDate.now().lengthOfMonth()
            val today = LocalDate.now()
            var streak = 0
            var day = today
            while (true) {
                val dayStr = day.toString()
                val dayTotal = allExpenses.filter { it.date == dayStr && it.type == "expense" }.sumOf { it.amount }
                if (dayTotal <= dailyTarget) {
                    streak++
                    day = day.minusDays(1)
                } else break
                if (streak > 365) break // safety cap
            }
            streak
        }
    }

    var showBalanceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with personalized greeting
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (userName.isNotBlank()) "Hey, ${userName.split(" ").first()}" else "Student",
                        style = MaterialTheme.typography.headlineMedium,
                        color = White
                    )
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                        color = SecondaryGrey
                    )
                }
                Text(
                    text = LocalDate.now().let {
                        "${it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}, ${it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${it.dayOfMonth}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryGrey
                )
            }
        }

        // Balance Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CURRENT BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(balance, currencySymbol),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = White
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showBalanceDialog = true }) {
                    Text(
                        text = "EDIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MidGrey,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "SPENT THIS MONTH",
                    value = formatCurrency(totalSpent, currencySymbol),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "TRANSACTIONS",
                    value = "${currentMonthExpenses.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily Avg stat
        item {
            StatCard(
                label = "AVERAGE DAILY SPEND",
                value = formatCurrency(avgDaily, currencySymbol),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Streak & Days Left
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "🔥 STREAK",
                    value = "$streakDays days",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "⏳ DAYS LEFT",
                    value = "$daysLeftInMonth",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gamification: Goal Achievement
        if (monthlyBudget > 0 && totalSpent <= monthlyBudget * 0.8 && currentMonthExpenses.isNotEmpty()) {
            item {
                GoalAchievementBanner()
            }
        }

        // Insights Carousel
        if (smartInsights.isNotEmpty()) {
            item {
                SectionHeader(title = "SMART INSIGHTS")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(smartInsights) { insight ->
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedGrey)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = insight,
                                style = MaterialTheme.typography.bodyMedium,
                                color = White
                            )
                        }
                    }
                }
            }
        }

        // Top Categories
        if (categoryTotals.isNotEmpty()) {
            item {
                SectionHeader(title = "TOP CATEGORIES")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryTotals.entries.toList()) { (category, amount) ->
                        CategoryPill(category = category, amount = amount, symbol = currencySymbol, viewModel = viewModel)
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "RECENT")
                TextButton(onClick = onSeeAllHistory) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodySmall,
                        color = MidGrey
                    )
                }
            }
        }

        if (recentExpenses.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidGrey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(recentExpenses, key = { it.id }) { expense ->
                TransactionItem(expense = expense, symbol = currencySymbol, viewModel = viewModel)
            }
        }

        // Bottom spacer for FAB
        item { Spacer(modifier = Modifier.height(56.dp)) }
    }

    // Balance Edit Dialog
    if (showBalanceDialog) {
        var balanceText by remember { mutableStateOf(balance.toBigDecimal().toPlainString()) }
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            containerColor = DarkGrey,
            title = { Text("Set Balance", color = White) },
            text = {
                TextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Balance ($currencySymbol)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ElevatedGrey,
                        unfocusedContainerColor = ElevatedGrey,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedLabelColor = SecondaryGrey,
                        unfocusedLabelColor = SecondaryGrey,
                        cursorColor = White,
                        focusedIndicatorColor = White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    balanceText.toDoubleOrNull()?.let { viewModel.setBalance(it) }
                    showBalanceDialog = false
                }) {
                    Text("Save", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) {
                    Text("Cancel", color = MidGrey)
                }
            }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGrey)
            .padding(18.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryGrey,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = White
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = SecondaryGrey,
        letterSpacing = 1.sp
    )
}

@Composable
fun CategoryPill(category: String, amount: Double, symbol: String, viewModel: BudgetViewModel) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGrey)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = viewModel.resolveCategoryIcon(category), fontSize = 22.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = viewModel.resolveCategoryLabel(category).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryGrey,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount, symbol),
            style = MaterialTheme.typography.titleMedium,
            color = White
        )
    }
}

@Composable
fun TransactionItem(
    expense: com.studentbudget.data.Expense,
    symbol: String,
    viewModel: BudgetViewModel,
    onDelete: (() -> Unit)? = null
) {
    val icon = viewModel.resolveCategoryIcon(expense.category)
    val label = viewModel.resolveCategoryLabel(expense.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGrey)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ElevatedGrey),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = White
            )
            if (expense.note.isNotBlank()) {
                Text(
                    text = expense.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Amount & Date
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "-${formatCurrency(expense.amount, symbol)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = White
            )
            Text(
                text = formatDateShort(expense.date),
                style = MaterialTheme.typography.labelSmall,
                color = MidGrey
            )
        }
        // Delete button (for history)
        if (onDelete != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDelete) {
                Text("✕", color = MidGrey, fontSize = 14.sp)
            }
        }
    }
}

fun formatCurrency(amount: Double, symbol: String = "$"): String {
    return "$symbol${String.format("%,.2f", amount)}"
}

fun formatDateShort(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                "$dow, $month ${date.dayOfMonth}"
            }
        }
    } catch (_: Exception) {
        dateStr
    }
}

@Composable
fun GoalAchievementBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(com.studentbudget.ui.theme.Success.copy(alpha = 0.2f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🎉", fontSize = 28.sp)
            Column {
                Text(
                    text = "Budget Hero!",
                    style = MaterialTheme.typography.titleMedium,
                    color = com.studentbudget.ui.theme.Success
                )
                Text(
                    text = "You're spending well under your monthly budget.",
                    style = MaterialTheme.typography.bodySmall,
                    color = White
                )
            }
        }
    }
}
