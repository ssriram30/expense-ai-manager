package com.expenseai.manager.data.local.dao

import androidx.room.*
import com.expenseai.manager.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses ORDER BY nextDueDate ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE nextDueDate <= :before ORDER BY nextDueDate ASC")
    fun getDueRecurringExpenses(before: Long): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getRecurringExpenseById(id: Long): RecurringExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long

    @Update
    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity)

    @Delete
    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity)
}
