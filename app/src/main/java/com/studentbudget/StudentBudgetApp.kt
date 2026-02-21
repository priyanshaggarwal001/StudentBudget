package com.studentbudget

import android.app.Application
import androidx.room.Room
import com.studentbudget.data.AppDatabase
import com.studentbudget.data.BudgetPreferences

class StudentBudgetApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferences: BudgetPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "studentbudget_db"
        )
            .fallbackToDestructiveMigration()
            .build()
        preferences = BudgetPreferences(applicationContext)
    }

    companion object {
        lateinit var instance: StudentBudgetApp
            private set
    }
}
