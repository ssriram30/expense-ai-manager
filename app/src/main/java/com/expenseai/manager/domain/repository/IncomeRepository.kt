package com.expenseai.manager.domain.repository

import com.expenseai.manager.domain.model.Income
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface IncomeRepository {
    fun getAllIncomes(): Flow<List<Income>>
    fun getIncomesByCurrency(currency: String): Flow<List<Income>>
    fun getIncomesByDateRange(start: Date, end: Date): Flow<List<Income>>
    suspend fun getIncomeById(id: Long): Income?
    suspend fun insertIncome(income: Income): Long
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(income: Income)
    fun getTotalIncomeByCurrency(currency: String, start: Date, end: Date): Flow<Double>
}
