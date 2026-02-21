package com.studentbudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentbudget.StudentBudgetApp
import com.studentbudget.data.Category
import com.studentbudget.data.CategoryEntity
import com.studentbudget.data.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class BudgetViewModel : ViewModel() {

    private val dao = StudentBudgetApp.instance.database.expenseDao()
    private val prefs = StudentBudgetApp.instance.preferences

    init {
        // Initialize default categories if needed and process recurring transactions
        viewModelScope.launch(Dispatchers.IO) {
            processRecurringTransactions()
        }
    }

    // ─── All Expenses ───
    val allExpenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Custom Categories ───
    val allCategories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Balance ───
    private val _balance = MutableStateFlow(prefs.balance)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // ─── Budget ───
    private val _monthlyBudget = MutableStateFlow(prefs.monthlyBudget)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    // ─── Settings / Preferences ───
    private val _currencySymbol = MutableStateFlow(prefs.currencySymbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _pinCode = MutableStateFlow(prefs.pinCode)
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    // ─── Selected Months for Analytics & History ───
    private val _analyticsMonth = MutableStateFlow(YearMonth.now())
    val analyticsMonth: StateFlow<YearMonth> = _analyticsMonth.asStateFlow()

    private val _historyMonth = MutableStateFlow(YearMonth.now())
    val historyMonth: StateFlow<YearMonth> = _historyMonth.asStateFlow()

    // ─── Profile Fields ───
    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow(prefs.userPhone)
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.userEmail)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userCollege = MutableStateFlow(prefs.userCollege)
    val userCollege: StateFlow<String> = _userCollege.asStateFlow()

    private val _userCourse = MutableStateFlow(prefs.userCourse)
    val userCourse: StateFlow<String> = _userCourse.asStateFlow()

    private val _profileImageUri = MutableStateFlow(prefs.profileImageUri)
    val profileImageUri: StateFlow<String> = _profileImageUri.asStateFlow()

    // ─── Recurring Engine ───
    private suspend fun processRecurringTransactions() {
        val today = LocalDate.now()
        val lastProcessedMillis = prefs.lastProcessedDate
        val lastProcessedDate = if (lastProcessedMillis == 0L) today else LocalDate.ofEpochDay(lastProcessedMillis)
        
        if (today.isAfter(lastProcessedDate)) {
            // Process recurring transactions logic would go here
            // For prototype, we just update the date
            prefs.lastProcessedDate = today.toEpochDay()
        }
    }

    // ─── Category CRUD ───
    fun saveCategory(nameKey: String, icon: String, label: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCategory(CategoryEntity(nameKey, icon, label, type))
        }
    }

    fun addCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCategory(category)
        }
    }

    fun deleteCategory(nameKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCategoryByKey(nameKey)
        }
    }

    // ─── Expense CRUD ───
    fun addExpense(
        amount: Double,
        category: String,
        note: String,
        date: LocalDate,
        type: String = "expense",
        recurringInterval: String = "none",
        paymentMethod: String = "Cash"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val expense = Expense(
                amount = amount,
                category = category,
                note = note,
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                type = type,
                recurringInterval = recurringInterval,
                paymentMethod = paymentMethod
            )
            dao.insert(expense)
            val diff = if (type == "expense") -amount else amount
            val newBalance = maxOf(0.0, _balance.value + diff)
            _balance.value = newBalance
            prefs.balance = newBalance
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(expense.id)
            val diff = if (expense.type == "expense") expense.amount else -expense.amount
            val newBalance = maxOf(0.0, _balance.value + diff)
            _balance.value = newBalance
            prefs.balance = newBalance
        }
    }

    fun updateExpense(old: Expense, newAmount: Double, newCategory: String, newNote: String, newDate: LocalDate, newType: String, newPaymentMethod: String = "Cash") {
        viewModelScope.launch(Dispatchers.IO) {
            // Reverse old balance effect
            val oldDiff = if (old.type == "expense") old.amount else -old.amount
            var bal = _balance.value + oldDiff

            // Apply new balance effect
            val newDiff = if (newType == "expense") -newAmount else newAmount
            bal = maxOf(0.0, bal + newDiff)

            val updated = old.copy(
                amount = newAmount,
                category = newCategory,
                note = newNote,
                date = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                type = newType,
                paymentMethod = newPaymentMethod
            )
            dao.update(updated)
            _balance.value = bal
            prefs.balance = bal
        }
    }

    // ─── Balance ───
    fun setBalance(value: Double) {
        _balance.value = value
        prefs.balance = value
    }

    fun addToBalance(amount: Double) {
        val newBalance = _balance.value + amount
        _balance.value = newBalance
        prefs.balance = newBalance
    }

    // ─── Settings ───
    fun setMonthlyBudget(value: Double) {
        _monthlyBudget.value = value
        prefs.monthlyBudget = value
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
        prefs.currencySymbol = symbol
    }

    fun setPinCode(pin: String) {
        _pinCode.value = pin
        prefs.pinCode = pin
    }

    fun factoryReset() {
        viewModelScope.launch(Dispatchers.IO) {
            // Clear preferences
            prefs.balance = 0.0
            prefs.monthlyBudget = 0.0
            prefs.currencySymbol = "$"
            prefs.pinCode = ""
            prefs.userName = ""
            prefs.userPhone = ""
            prefs.userEmail = ""
            prefs.userCollege = ""
            prefs.userCourse = ""
            prefs.profileImageUri = ""
            prefs.lastProcessedDate = 0L

            // Destructive clear DB (for prototype: clean start)
            StudentBudgetApp.instance.database.clearAllTables()

            _balance.value = 0.0
            _monthlyBudget.value = 0.0
            _currencySymbol.value = "$"
            _pinCode.value = ""
            _userName.value = ""
            _userPhone.value = ""
            _userEmail.value = ""
            _userCollege.value = ""
            _userCourse.value = ""
            _profileImageUri.value = ""
        }
    }

    // ─── Profile Setters ───
    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        college: String,
        course: String
    ) {
        _userName.value = name
        _userPhone.value = phone
        _userEmail.value = email
        _userCollege.value = college
        _userCourse.value = course
        prefs.userName = name
        prefs.userPhone = phone
        prefs.userEmail = email
        prefs.userCollege = college
        prefs.userCourse = course
    }

    fun setProfileImage(uri: String) {
        _profileImageUri.value = uri
        prefs.profileImageUri = uri
    }

    // ─── Month Navigation ───
    fun shiftAnalyticsMonth(delta: Long) {
        _analyticsMonth.value = _analyticsMonth.value.plusMonths(delta)
    }

    fun shiftHistoryMonth(delta: Long) {
        _historyMonth.value = _historyMonth.value.plusMonths(delta)
    }

    // ─── Helpers ───
    fun getMonthExpenses(expenses: List<Expense>, yearMonth: YearMonth): List<Expense> {
        val prefix = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return expenses.filter { it.date.startsWith(prefix) }
    }

    fun getCurrentMonthExpenses(expenses: List<Expense>): List<Expense> {
        return getMonthExpenses(expenses, YearMonth.now())
    }

    fun getCategoryTotals(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toSortedMap(compareByDescending { cat ->
                expenses.filter { it.category == cat }.sumOf { it.amount }
            })
    }

    fun getTotalSpentAllTime(expenses: List<Expense>): Double {
        return expenses.filter { it.type == "expense" }.sumOf { it.amount }
    }

    fun getAverageDailySpending(expenses: List<Expense>): Double {
        val ext = expenses.filter { it.type == "expense" }
        if (ext.isEmpty()) return 0.0
        val dates = ext.mapNotNull {
            try { LocalDate.parse(it.date) } catch (_: Exception) { null }
        }
        if (dates.isEmpty()) return 0.0
        val earliest = dates.minOrNull()!!
        val latest = dates.maxOrNull()!!
        val daySpan = ChronoUnit.DAYS.between(earliest, latest) + 1
        return ext.sumOf { it.amount } / daySpan
    }

    fun getExpenseCount(expenses: List<Expense>): Int = expenses.size

    fun formatMonthLabel(yearMonth: YearMonth): String {
        val month = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$month ${yearMonth.year}"
    }

    // ─── Smart Insights ───
    val smartInsights: StateFlow<List<String>> = combine(allExpenses, monthlyBudget) { expenses, budget ->
        val currentMonthExpenses = getCurrentMonthExpenses(expenses)
        val expensesOnly = currentMonthExpenses.filter { it.type == "expense" }
        val spent = expensesOnly.sumOf { it.amount }
        val insights = mutableListOf<String>()

        if (budget > 0) {
            val pct = spent / budget
            if (pct >= 0.9) {
                insights.add("⚠️ You've spent ${(pct * 100).toInt()}% of your budget this month!")
            } else if (pct >= 0.5) {
                insights.add("📊 You've spent half of your budget.")
            } else if (spent > 0) {
                insights.add("🌟 Great job! You are well within your budget.")
            }
        }
        
        val categoryTotals = getCategoryTotals(currentMonthExpenses)
        if (categoryTotals.isNotEmpty()) {
            val topCategory = categoryTotals.maxByOrNull { it.value }
            if (topCategory != null && topCategory.value > 0) {
                 insights.add("💡 Top spending category: ${resolveCategoryLabel(topCategory.key)} (${resolveCategoryIcon(topCategory.key)})")
            }
        }
        
        val todayExpenses = expenses.filter { it.date == LocalDate.now().toString() }
        if (todayExpenses.isEmpty() && expenses.isNotEmpty()) {
            insights.add("🎉 No expenses so far today. Keep it up!")
        }

        if (insights.isEmpty()) {
            insights.add("Welcome! Start adding expenses to see insights here.")
        }
        insights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Temporary Fallback resolver for UI until custom categories are full
    fun resolveCategoryIcon(categoryKey: String): String {
        val custom = allCategories.value.find { it.nameKey == categoryKey }
        if (custom != null) return custom.icon
        return Category.fromKey(categoryKey).icon
    }

    fun resolveCategoryLabel(categoryKey: String): String {
        val custom = allCategories.value.find { it.nameKey == categoryKey }
        if (custom != null) return custom.label
        return Category.fromKey(categoryKey).label
    }

    // ─── Backup & Restore ───
    fun exportDataToJson(): String {
        val root = org.json.JSONObject()
        
        // Expenses
        val expensesArray = org.json.JSONArray()
        for (exp in allExpenses.value) {
            val obj = org.json.JSONObject()
            obj.put("amount", exp.amount)
            obj.put("category", exp.category)
            obj.put("date", exp.date)
            obj.put("note", exp.note)
            obj.put("type", exp.type)
            obj.put("paymentMethod", exp.paymentMethod)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)
        
        // Categories
        val categoriesArray = org.json.JSONArray()
        for (cat in allCategories.value) {
            val obj = org.json.JSONObject()
            obj.put("nameKey", cat.nameKey)
            obj.put("icon", cat.icon)
            obj.put("label", cat.label)
            obj.put("type", cat.type)
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)
        
        // Settings
        val prefsObj = org.json.JSONObject()
        prefsObj.put("balance", prefs.balance)
        prefsObj.put("monthlyBudget", prefs.monthlyBudget)
        prefsObj.put("userName", prefs.userName)
        prefsObj.put("userPhone", prefs.userPhone)
        prefsObj.put("userEmail", prefs.userEmail)
        prefsObj.put("userCollege", prefs.userCollege)
        prefsObj.put("userCourse", prefs.userCourse)
        prefsObj.put("currencySymbol", prefs.currencySymbol)
        root.put("preferences", prefsObj)
        
        return root.toString()
    }

    fun importDataFromJson(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = org.json.JSONObject(jsonString)
                
                // Clear existing db and prefs
                factoryReset()
                
                // Expenses
                val expensesArray = root.optJSONArray("expenses")
                if (expensesArray != null) {
                    for (i in 0 until expensesArray.length()) {
                        val obj = expensesArray.getJSONObject(i)
                        dao.insert(
                            Expense(
                                amount = obj.getDouble("amount"),
                                category = obj.getString("category"),
                                date = obj.getString("date"),
                                note = obj.optString("note", ""),
                                type = obj.optString("type", "expense"),
                                paymentMethod = obj.optString("paymentMethod", "Cash")
                            )
                        )
                    }
                }
                
                // Categories
                val categoriesArray = root.optJSONArray("categories")
                if (categoriesArray != null) {
                    for (i in 0 until categoriesArray.length()) {
                        val obj = categoriesArray.getJSONObject(i)
                        dao.insertCategory(
                            CategoryEntity(
                                nameKey = obj.getString("nameKey"),
                                icon = obj.getString("icon"),
                                label = obj.getString("label"),
                                type = obj.optString("type", "expense")
                            )
                        )
                    }
                }
                
                // Preferences
                val prefsObj = root.optJSONObject("preferences")
                if (prefsObj != null) {
                    if (prefsObj.has("balance")) setBalance(prefsObj.getDouble("balance"))
                    if (prefsObj.has("monthlyBudget")) setMonthlyBudget(prefsObj.getDouble("monthlyBudget"))
                    if (prefsObj.has("userName")) _userName.value = prefsObj.getString("userName").also { prefs.userName = it }
                    if (prefsObj.has("userPhone")) _userPhone.value = prefsObj.getString("userPhone").also { prefs.userPhone = it }
                    if (prefsObj.has("userEmail")) _userEmail.value = prefsObj.getString("userEmail").also { prefs.userEmail = it }
                    if (prefsObj.has("userCollege")) _userCollege.value = prefsObj.getString("userCollege").also { prefs.userCollege = it }
                    if (prefsObj.has("userCourse")) _userCourse.value = prefsObj.getString("userCourse").also { prefs.userCourse = it }
                    if (prefsObj.has("currencySymbol")) _currencySymbol.value = prefsObj.getString("currencySymbol").also { prefs.currencySymbol = it }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
