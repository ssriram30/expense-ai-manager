package com.expenseai.manager.data.local.dao

import androidx.room.*
import com.expenseai.manager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE currency = :currency ORDER BY date DESC")
    fun getExpensesByCurrency(currency: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE currency = :currency AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesByCurrencyAndDateRange(currency: String, start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE merchant = :merchant ORDER BY date DESC")
    fun getExpensesByMerchant(merchant: String): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR merchant LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses
        WHERE currency = :currency AND date BETWEEN :start AND :end AND type = 'EXPENSE'
    """)
    fun getTotalExpensesByCurrency(currency: String, start: Long, end: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT COUNT(*) FROM expenses")
    fun getExpenseCount(): Flow<Int>
}
