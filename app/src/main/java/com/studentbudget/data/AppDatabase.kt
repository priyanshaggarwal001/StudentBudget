package com.studentbudget.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Expense::class, CategoryEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
