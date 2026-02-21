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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.data.Expense
import com.studentbudget.ui.theme.Black
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import java.time.LocalDate

@Composable
fun HistoryScreen(viewModel: BudgetViewModel, onEditExpense: (Expense) -> Unit = {}) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val historyMonth by viewModel.historyMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val monthExpenses = viewModel.getMonthExpenses(allExpenses, historyMonth)

    val filteredExpenses = remember(monthExpenses, searchQuery, selectedCategory) {
        monthExpenses.filter { ext ->
            val matchQuery = if (searchQuery.isNotBlank()) {
                ext.note.contains(searchQuery, ignoreCase = true) ||
                viewModel.resolveCategoryLabel(ext.category).contains(searchQuery, ignoreCase = true)
            } else true
            
            val matchCategory = if (selectedCategory != null) {
                ext.category == selectedCategory
            } else true
            
            matchQuery && matchCategory
        }
    }

    val availableCategories = remember(monthExpenses) {
        monthExpenses.map { it.category }.distinct()
    }

    // Group by date
    val grouped = filteredExpenses.groupBy { it.date }.toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineLarge,
                    color = White
                )
                MonthPicker(
                    label = viewModel.formatMonthLabel(historyMonth),
                    onPrev = { viewModel.shiftHistoryMonth(-1) },
                    onNext = { viewModel.shiftHistoryMonth(1) }
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholder = { Text("Search by note or category...", color = MidGrey) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MidGrey) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MidGrey)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ElevatedGrey,
                    unfocusedContainerColor = ElevatedGrey,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        // Filter Pills
        if (availableCategories.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterPill(
                            label = "All",
                            isSelected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(availableCategories) { cat ->
                        FilterPill(
                            label = viewModel.resolveCategoryLabel(cat),
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
            }
        }

        if (grouped.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidGrey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            grouped.forEach { (date, expenses) ->
                item {
                    Text(
                        text = formatDateShort(date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MidGrey,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(expenses, key = { it.id }) { expense ->
                    HistoryTransactionItem(
                        expense = expense,
                        symbol = currencySymbol,
                        viewModel = viewModel,
                        onEdit = { onEditExpense(expense) },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }
}

@Composable
fun HistoryTransactionItem(expense: Expense, symbol: String, viewModel: BudgetViewModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val icon = viewModel.resolveCategoryIcon(expense.category)
    val label = viewModel.resolveCategoryLabel(expense.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGrey)
            .clickable { onEdit() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(horizontalAlignment = Alignment.End) {
            val isIncome = expense.type == "income"
            Text(
                text = "${if (isIncome) "+" else "-"}${formatCurrency(expense.amount, symbol)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isIncome) com.studentbudget.ui.theme.Success else White
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(onClick = onEdit) {
            Text("✏️", fontSize = 14.sp)
        }
        TextButton(onClick = onDelete) {
            Text("✕", color = MidGrey, fontSize = 14.sp)
        }
    }
}

@Composable
fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) White else DarkGrey)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Black else SecondaryGrey
        )
    }
}
