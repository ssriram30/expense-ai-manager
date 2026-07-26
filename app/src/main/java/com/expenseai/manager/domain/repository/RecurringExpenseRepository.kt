package com.expenseai.manager.domain.repository

import com.expenseai.manager.domain.model.RecurringExpense
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface RecurringExpenseRepository {
    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>
    fun getDueRecurringExpenses(before: Date): Flow<List<RecurringExpense>>
    suspend fun getRecurringExpenseById(id: Long): RecurringExpense?
    suspend fun insertRecurringExpense(expense: RecurringExpense): Long
    suspend fun updateRecurringExpense(expense: RecurringExpense)
    suspend fun deleteRecurringExpense(expense: RecurringExpense)
}
