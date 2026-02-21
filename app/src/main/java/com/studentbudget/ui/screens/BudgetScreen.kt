package com.studentbudget.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.Danger
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.Success
import com.studentbudget.ui.theme.Warning
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import java.time.YearMonth

@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val budget by viewModel.monthlyBudget.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val currentMonthExpenses = viewModel.getCurrentMonthExpenses(allExpenses)
    val expensesOnly = currentMonthExpenses.filter { it.type == "expense" }
    val spent = expensesOnly.sumOf { it.amount }
    val remaining = maxOf(0.0, budget - spent)
    val pct = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1.5f) else 0f
    val displayPct = pct.coerceAtMost(1f)
    val categoryTotals = viewModel.getCategoryTotals(expensesOnly)

    var showBudgetDialog by remember { mutableStateOf(false) }

    // Animate progress bar
    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(displayPct) { targetProgress = displayPct }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    val progressColor = when {
        pct >= 1f -> Danger
        pct >= 0.75f -> Warning
        else -> White
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Budget",
                style = MaterialTheme.typography.headlineLarge,
                color = White
            )
        }

        // Budget Limit Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONTHLY LIMIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryGrey,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = { showBudgetDialog = true }) {
                        Text(
                            text = if (budget > 0) "EDIT" else "SET",
                            style = MaterialTheme.typography.labelSmall,
                            color = MidGrey,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Text(
                    text = if (budget > 0) formatCurrency(budget, currencySymbol) else "Not set",
                    style = MaterialTheme.typography.displayMedium,
                    color = White
                )
            }
        }

        // Progress Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SPENT", style = MaterialTheme.typography.labelSmall, color = SecondaryGrey, letterSpacing = 1.sp)
                    Text(formatCurrency(spent, currencySymbol), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = White)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElevatedGrey)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(progressColor)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("REMAINING", style = MaterialTheme.typography.labelSmall, color = SecondaryGrey, letterSpacing = 1.sp)
                    Text(
                        text = if (budget > 0) formatCurrency(remaining, currencySymbol) else "—",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (budget > 0 && spent >= budget) Danger else Success
                    )
                }
            }
        }

        // Category Breakdown
        if (categoryTotals.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkGrey)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "CATEGORY BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryGrey,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val maxVal = if (budget > 0) budget else (categoryTotals.values.maxOrNull() ?: 1.0)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        categoryTotals.entries.forEach { (cat, amount) ->
                            BarChartRow(
                                icon = viewModel.resolveCategoryIcon(cat),
                                label = viewModel.resolveCategoryLabel(cat),
                                amount = amount,
                                symbol = currencySymbol,
                                fraction = (amount / maxVal).toFloat().coerceIn(0.02f, 1f),
                                color = White
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }

    // Budget Dialog
    if (showBudgetDialog) {
        var budgetText by remember { mutableStateOf(if (budget > 0) budget.toInt().toString() else "") }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = DarkGrey,
            title = { Text("Set Monthly Budget", color = White) },
            text = {
                TextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Monthly Limit ($currencySymbol)") },
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
                    budgetText.toDoubleOrNull()?.let { if (it > 0) viewModel.setMonthlyBudget(it) }
                    showBudgetDialog = false
                }) {
                    Text("Save", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel", color = MidGrey)
                }
            }
        )
    }
}
