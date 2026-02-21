package com.studentbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfileScreen(viewModel: BudgetViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userCollege by viewModel.userCollege.collectAsState()
    val userCourse by viewModel.userCourse.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val totalSpent = remember(allExpenses) { viewModel.getTotalSpentAllTime(allExpenses) }
    val avgDaily = remember(allExpenses) { viewModel.getAverageDailySpending(allExpenses) }
    val txCount = remember(allExpenses) { viewModel.getExpenseCount(allExpenses) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val pinCode by viewModel.pinCode.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take persistable permission so the URI survives reboots
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setProfileImage(it.toString())
        }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val json = viewModel.exportDataToJson()
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(json.toByteArray())
                }
                Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    val json = input.bufferedReader().use { r -> r.readText() }
                    viewModel.importDataFromJson(json)
                }
                Toast.makeText(context, "Restore successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = White
            )
        }

        // Avatar + Name Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle — tap to pick image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ElevatedGrey)
                        .clickable { imagePickerLauncher.launch(arrayOf("image/*")) },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(profileImageUri)),
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = if (userName.isNotBlank()) {
                            userName.split(" ")
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                        } else "?"
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Tap photo to change",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidGrey
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = userName.ifBlank { "Tap Edit to set name" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (userName.isNotBlank()) White else MidGrey,
                    textAlign = TextAlign.Center
                )
                if (userCourse.isNotBlank()) {
                    Text(
                        text = userCourse,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGrey
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { showEditDialog = true }) {
                    Text(
                        text = "EDIT PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MidGrey,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Details Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                ProfileRow(label = "📱  Phone", value = userPhone.ifBlank { "Not set" })
                ProfileRow(label = "✉️  Email", value = userEmail.ifBlank { "Not set" })
                ProfileRow(label = "🏫  College", value = userCollege.ifBlank { "Not set" })
                ProfileRow(label = "📖  Course", value = userCourse.ifBlank { "Not set" })
            }
        }

        // Quick Stats Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp)
            ) {
                Text(
                    text = "QUICK STATS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "BALANCE",
                        value = formatCurrency(balance, currencySymbol),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "ALL-TIME SPENT",
                        value = formatCurrency(totalSpent, currencySymbol),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "AVG / DAY",
                        value = formatCurrency(avgDaily, currencySymbol),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "TRANSACTIONS",
                        value = "$txCount",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Settings Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrey,
                    letterSpacing = 1.sp
                )
                
                // Add Custom Category
                SettingsRow(
                    label = "Add Custom Category",
                    value = "",
                    onClick = { showCategoryDialog = true }
                )

                // Currency
                SettingsRow(
                    label = "Currency Symbol",
                    value = currencySymbol,
                    onClick = { showCurrencyDialog = true }
                )
                
                // PIN Lock
                SettingsRow(
                    label = "App PIN Lock",
                    value = if (pinCode.isNotBlank()) "Enabled" else "Disabled",
                    onClick = { showPinDialog = true }
                )
                
                // Backup
                SettingsRow(
                    label = "Backup Data",
                    value = "",
                    onClick = { exportLauncher.launch("budget_backup.json") }
                )
                
                // Restore
                SettingsRow(
                    label = "Restore Data (JSON)",
                    value = "",
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                )
                
                // Factory Reset
                SettingsRow(
                    label = "Clear All Data",
                    value = "",
                    isDestructive = true,
                    onClick = { showClearDataDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }

    // Add Custom Category Dialog
    if (showCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catIcon by remember { mutableStateOf("🎓") }
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = DarkGrey,
            title = { Text("New Category", color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileTextField(value = catIcon, onValueChange = { catIcon = it }, label = "Icon (emoji)")
                    ProfileTextField(value = catName, onValueChange = { catName = it }, label = "Category Name")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (catName.isNotBlank()) {
                        val key = catName.trim().uppercase().replace(" ", "_")
                        viewModel.addCategory(
                            com.studentbudget.data.CategoryEntity(
                                nameKey = key,
                                icon = catIcon.ifBlank { "📌" },
                                label = catName.trim(),
                                type = "expense"
                            )
                        )
                    }
                    showCategoryDialog = false
                }) { Text("Add", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("Cancel", color = MidGrey) }
            }
        )
    }

    // Currency Dialog
    if (showCurrencyDialog) {
        var newCurrency by remember { mutableStateOf(currencySymbol) }
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = DarkGrey,
            title = { Text("Set Currency Symbol", color = White) },
            text = {
                ProfileTextField(value = newCurrency, onValueChange = { newCurrency = it }, label = "Symbol (e.g. $, €, ₹)")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCurrency.isNotBlank()) viewModel.setCurrencySymbol(newCurrency.trim())
                    showCurrencyDialog = false
                }) { Text("Save", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel", color = MidGrey) }
            }
        )
    }

    // PIN Dialog
    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        val isRemoving = pinCode.isNotBlank()
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = DarkGrey,
            title = { Text(if (isRemoving) "Remove PIN" else "Set 4-Digit PIN", color = White) },
            text = {
                if (isRemoving) {
                    Text("Are you sure you want to disable the PIN lock?", color = SecondaryGrey)
                } else {
                    ProfileTextField(value = newPin, onValueChange = { if (it.length <= 4) newPin = it.filter { c -> c.isDigit() } }, label = "New PIN")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isRemoving) {
                        viewModel.setPinCode("")
                    } else if (newPin.length == 4) {
                        viewModel.setPinCode(newPin)
                    }
                    showPinDialog = false
                }) { Text(if (isRemoving) "Remove" else "Save", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel", color = MidGrey) }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = DarkGrey,
            title = { Text("Clear All Data", color = com.studentbudget.ui.theme.Danger) },
            text = { Text("Are you sure you want to permanently delete all transactions, categories, and settings?", color = SecondaryGrey) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.factoryReset()
                    showClearDataDialog = false
                }) { Text("Delete Everything", color = com.studentbudget.ui.theme.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel", color = MidGrey) }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(userName) }
        var editPhone by remember { mutableStateOf(userPhone) }
        var editEmail by remember { mutableStateOf(userEmail) }
        var editCollege by remember { mutableStateOf(userCollege) }
        var editCourse by remember { mutableStateOf(userCourse) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = DarkGrey,
            title = { Text("Edit Profile", color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileTextField(value = editName, onValueChange = { editName = it }, label = "Full Name")
                    ProfileTextField(value = editPhone, onValueChange = { editPhone = it }, label = "Phone Number")
                    ProfileTextField(value = editEmail, onValueChange = { editEmail = it }, label = "Email Address")
                    ProfileTextField(value = editCollege, onValueChange = { editCollege = it }, label = "College / University")
                    ProfileTextField(value = editCourse, onValueChange = { editCourse = it }, label = "Course / Major")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(
                        name = editName.trim(),
                        phone = editPhone.trim(),
                        email = editEmail.trim(),
                        college = editCollege.trim(),
                        course = editCourse.trim()
                    )
                    showEditDialog = false
                }) {
                    Text("Save", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = MidGrey)
                }
            }
        )
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryGrey
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (value == "Not set") MidGrey else White
        )
    }
}

@Composable
fun ProfileTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ElevatedGrey,
            unfocusedContainerColor = ElevatedGrey,
            focusedTextColor = White,
            unfocusedTextColor = White,
            focusedLabelColor = SecondaryGrey,
            unfocusedLabelColor = SecondaryGrey,
            cursorColor = White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}


@Composable
fun SettingsRow(label: String, value: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDestructive) com.studentbudget.ui.theme.Danger else White
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SecondaryGrey
            )
        }
    }
}
