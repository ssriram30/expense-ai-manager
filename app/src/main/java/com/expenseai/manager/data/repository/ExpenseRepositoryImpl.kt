package com.expenseai.manager.data.repository

import com.expenseai.manager.data.local.dao.ExpenseDao
import com.expenseai.manager.data.local.entity.ExpenseEntity
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { it.map(ExpenseEntity::toDomain) }

    override fun getExpensesByCurrency(currency: String): Flow<List<Expense>> =
        dao.getExpensesByCurrency(currency).map { it.map(ExpenseEntity::toDomain) }

    override fun getExpensesByDateRange(start: Date, end: Date): Flow<List<Expense>> =
        dao.getExpensesByDateRange(start.time, end.time).map { it.map(ExpenseEntity::toDomain) }

    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> =
        dao.getExpensesByCategory(category.name).map { it.map(ExpenseEntity::toDomain) }

    override fun getExpensesByCurrencyAndDateRange(
        currency: String, start: Date, end: Date
    ): Flow<List<Expense>> =
        dao.getExpensesByCurrencyAndDateRange(currency, start.time, end.time)
            .map { it.map(ExpenseEntity::toDomain) }

    override fun searchExpenses(query: String): Flow<List<Expense>> =
        dao.searchExpenses(query).map { it.map(ExpenseEntity::toDomain) }

    override suspend fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)?.toDomain()

    override suspend fun insertExpense(expense: Expense): Long =
        dao.insertExpense(ExpenseEntity.fromDomain(expense))

    override suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(ExpenseEntity.fromDomain(expense))

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(ExpenseEntity.fromDomain(expense))

    override suspend fun deleteExpenseById(id: Long) =
        dao.deleteExpenseById(id)

    override fun getTotalExpensesByCurrency(currency: String, start: Date, end: Date): Flow<Double> =
        dao.getTotalExpensesByCurrency(currency, start.time, end.time)

    override fun getExpensesByMerchant(merchant: String): Flow<List<Expense>> =
        dao.getExpensesByMerchant(merchant).map { it.map(ExpenseEntity::toDomain) }
}
