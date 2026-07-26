package com.expenseai.manager.data.repository

import com.expenseai.manager.data.local.dao.RecurringExpenseDao
import com.expenseai.manager.data.local.entity.RecurringExpenseEntity
import com.expenseai.manager.domain.model.RecurringExpense
import com.expenseai.manager.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class RecurringExpenseRepositoryImpl @Inject constructor(
    private val dao: RecurringExpenseDao
) : RecurringExpenseRepository {

    override fun getAllRecurringExpenses(): Flow<List<RecurringExpense>> =
        dao.getAllRecurringExpenses().map { it.map(RecurringExpenseEntity::toDomain) }

    override fun getDueRecurringExpenses(before: Date): Flow<List<RecurringExpense>> =
        dao.getDueRecurringExpenses(before.time).map { it.map(RecurringExpenseEntity::toDomain) }

    override suspend fun getRecurringExpenseById(id: Long): RecurringExpense? =
        dao.getRecurringExpenseById(id)?.toDomain()

    override suspend fun insertRecurringExpense(expense: RecurringExpense): Long =
        dao.insertRecurringExpense(RecurringExpenseEntity.fromDomain(expense))

    override suspend fun updateRecurringExpense(expense: RecurringExpense) =
        dao.updateRecurringExpense(RecurringExpenseEntity.fromDomain(expense))

    override suspend fun deleteRecurringExpense(expense: RecurringExpense) =
        dao.deleteRecurringExpense(RecurringExpenseEntity.fromDomain(expense))
}
