package com.expenseai.manager.data.repository

import com.expenseai.manager.data.local.dao.BudgetDao
import com.expenseai.manager.data.local.entity.BudgetEntity
import com.expenseai.manager.domain.model.Budget
import com.expenseai.manager.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<Budget>> =
        dao.getAllBudgets().map { it.map(BudgetEntity::toDomain) }

    override fun getBudgetsByMonthYear(month: Int, year: Int): Flow<List<Budget>> =
        dao.getBudgetsByMonthYear(month, year).map { it.map(BudgetEntity::toDomain) }

    override suspend fun getBudgetById(id: Long): Budget? =
        dao.getBudgetById(id)?.toDomain()

    override suspend fun insertBudget(budget: Budget): Long =
        dao.insertBudget(BudgetEntity.fromDomain(budget))

    override suspend fun updateBudget(budget: Budget) =
        dao.updateBudget(BudgetEntity.fromDomain(budget))

    override suspend fun deleteBudget(budget: Budget) =
        dao.deleteBudget(BudgetEntity.fromDomain(budget))
}
