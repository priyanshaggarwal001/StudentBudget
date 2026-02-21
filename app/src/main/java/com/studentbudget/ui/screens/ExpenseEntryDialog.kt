package com.studentbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.Black
import com.studentbudget.ui.theme.Danger
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.ElevatedGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.SecondaryGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import com.studentbudget.data.Expense
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseEntryDialog(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    editExpense: Expense? = null
) {
    val isEditing = editExpense != null
    var amount by remember { mutableStateOf(editExpense?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(editExpense?.category ?: "FOOD") }
    var note by remember { mutableStateOf(editExpense?.note ?: "") }
    var date by remember { mutableStateOf(
        if (editExpense != null) try { LocalDate.parse(editExpense.date) } catch (_: Exception) { LocalDate.now() }
        else LocalDate.now()
    ) }
    var showError by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(editExpense?.type == "income") }
    var selectedPaymentMethod by remember { mutableStateOf(editExpense?.paymentMethod ?: "Cash") }
    val context = LocalContext.current

    val paymentMethods = listOf(
        "💵" to "Cash",
        "📱" to "UPI",
        "💳" to "Card",
        "🏦" to "Bank Transfer",
        "📦" to "Other"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(DarkGrey)
                .pointerInput(Unit) { detectTapGestures { /* consume taps */ } }
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEditing) (if (isIncome) "Edit Income" else "Edit Expense")
                           else (if (isIncome) "Add Income" else "Add Expense"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = White,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("✕", color = SecondaryGrey, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expense / Income Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ElevatedGrey)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isIncome) White else Color.Transparent)
                        .clickable { isIncome = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isIncome) Black else SecondaryGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) White else Color.Transparent)
                        .clickable { isIncome = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isIncome) Black else SecondaryGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount
            Text(
                text = "AMOUNT ($)",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = amount,
                onValueChange = {
                    amount = it
                    showError = false
                },
                placeholder = { Text("0.00", color = MidGrey) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                isError = showError,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ElevatedGrey,
                    unfocusedContainerColor = ElevatedGrey,
                    errorContainerColor = ElevatedGrey,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    errorTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Danger
                )
            )
            if (showError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please enter a valid amount greater than 0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category
            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Combine default and custom categories
            val customCategories by viewModel.allCategories.collectAsState()
            val defaultCategories = listOf(
                Triple("FOOD", "🍔", "Food"), 
                Triple("TRANSPORT", "🚌", "Transport"), 
                Triple("TEXTBOOKS", "📚", "Textbooks"), 
                Triple("RENT", "🏠", "Rent"), 
                Triple("ENTERTAINMENT", "🎮", "Fun"),
                Triple("ALLOWANCE", "💌", "Allowance"),
                Triple("JOB", "💼", "Job"),
                Triple("OTHER", "📦", "Other"),
            )
            val allAvailableCategories = defaultCategories + customCategories.map { cat -> Triple(cat.nameKey, cat.icon, cat.label) }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                allAvailableCategories.forEach { (catKey, catIcon, catLabel) ->
                    val isSelected = catKey == selectedCategory
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) White.copy(alpha = 0.06f) else ElevatedGrey)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) White else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = catKey }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = catIcon.toString(), fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = catLabel.toString().uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) White else SecondaryGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note
            Text(
                text = "NOTE (OPTIONAL)",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = note,
                onValueChange = { if (it.length <= 60) note = it },
                placeholder = { Text("e.g. Coffee at campus cafe", color = MidGrey) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ElevatedGrey,
                    unfocusedContainerColor = ElevatedGrey,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Method
            Text(
                text = "PAYMENT METHOD",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { (icon, label) ->
                    val isSelected = selectedPaymentMethod == label
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) White.copy(alpha = 0.06f) else ElevatedGrey)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMethod = label }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected) White else SecondaryGrey,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Picker
            Text(
                text = "DATE",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryGrey,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ElevatedGrey)
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                            date.year, date.monthValue - 1, date.dayOfMonth
                        ).show()
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        val type = if (isIncome) "income" else "expense"
                        if (isEditing && editExpense != null) {
                            viewModel.updateExpense(
                                old = editExpense,
                                newAmount = amt,
                                newCategory = selectedCategory,
                                newNote = note.trim(),
                                newDate = date,
                                newType = type,
                                newPaymentMethod = selectedPaymentMethod
                            )
                        } else {
                            viewModel.addExpense(
                                amount = amt,
                                category = selectedCategory,
                                note = note.trim(),
                                date = date,
                                type = type,
                                paymentMethod = selectedPaymentMethod
                            )
                        }
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) com.studentbudget.ui.theme.Success else White,
                    contentColor = if (isIncome) White else Black
                )
            ) {
                Text(
                    text = if (isEditing) "SAVE CHANGES" else (if (isIncome) "ADD INCOME" else "ADD EXPENSE"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
