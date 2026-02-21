package com.studentbudget.data

import android.content.Context
import android.content.SharedPreferences

class BudgetPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("studentbudget_prefs", Context.MODE_PRIVATE)

    var balance: Double
        get() = prefs.getFloat(KEY_BALANCE, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_BALANCE, value.toFloat()).apply()

    var monthlyBudget: Double
        get() = prefs.getFloat(KEY_BUDGET, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_BUDGET, value.toFloat()).apply()

    // --- Premium Features ---
    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    var pinCode: String
        get() = prefs.getString(KEY_PIN_CODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

    var lastProcessedDate: Long
        get() = prefs.getLong(KEY_LAST_PROCESSED_DATE, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_PROCESSED_DATE, value).apply()
        
    // ─── Profile Fields ───
    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String
        get() = prefs.getString(KEY_USER_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userCollege: String
        get() = prefs.getString(KEY_USER_COLLEGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_COLLEGE, value).apply()

    var userCourse: String
        get() = prefs.getString(KEY_USER_COURSE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_COURSE, value).apply()

    var profileImageUri: String
        get() = prefs.getString(KEY_PROFILE_IMAGE_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PROFILE_IMAGE_URI, value).apply()

    companion object {
        private const val KEY_BALANCE = "balance"
        private const val KEY_BUDGET = "monthly_budget"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_LAST_PROCESSED_DATE = "last_processed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_COLLEGE = "user_college"
        private const val KEY_USER_COURSE = "user_course"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
    }
}
