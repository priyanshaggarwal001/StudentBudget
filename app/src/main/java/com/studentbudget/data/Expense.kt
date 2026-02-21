package com.studentbudget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String, // foreign key relation to CategoryEntity.nameKey
    val note: String = "",
    val date: String, // yyyy-MM-dd format
    val type: String = "expense", // "expense" or "income"
    val recurringInterval: String = "none", // "none", "daily", "weekly", "monthly"
    val paymentMethod: String = "Cash" // "Cash", "UPI", "Card", "Bank Transfer", "Other"
)

enum class Category(val icon: String, val label: String) {
    FOOD("🍔", "Food"),
    TRANSPORT("🚌", "Transport"),
    TEXTBOOKS("📚", "Textbooks"),
    RENT("🏠", "Rent"),
    ENTERTAINMENT("🎮", "Fun"),
    ALLOWANCE("💌", "Allowance"),
    JOB("💼", "Job"),
    OTHER("📦", "Other");

    companion object {
        fun fromKey(key: String): Category {
            return entries.find { it.name.equals(key, ignoreCase = true) } ?: OTHER
        }
    }
}
