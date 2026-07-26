package com.expenseai.manager.domain.repository

import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByCurrency(currency: String): Flow<List<Expense>>
    fun getExpensesByDateRange(start: Date, end: Date): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    fun getExpensesByCurrencyAndDateRange(currency: String, start: Date, end: Date): Flow<List<Expense>>
    fun searchExpenses(query: String): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
    fun getTotalExpensesByCurrency(currency: String, start: Date, end: Date): Flow<Double>
    fun getExpensesByMerchant(merchant: String): Flow<List<Expense>>
}
