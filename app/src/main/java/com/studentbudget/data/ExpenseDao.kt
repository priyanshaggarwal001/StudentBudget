package com.studentbudget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date LIKE :monthPattern ORDER BY date DESC, id DESC")
    fun getExpensesByMonth(monthPattern: String): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date LIKE :monthPattern AND type = 'expense'")
    fun getMonthlyTotal(monthPattern: String): Flow<Double>

    // --- Category Entities ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE nameKey = :key")
    suspend fun deleteCategoryByKey(key: String)

    @Query("SELECT * FROM categories ORDER BY label ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
}
