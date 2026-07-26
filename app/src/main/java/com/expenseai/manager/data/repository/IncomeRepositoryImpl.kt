package com.expenseai.manager.data.repository

import com.expenseai.manager.data.local.dao.IncomeDao
import com.expenseai.manager.data.local.entity.IncomeEntity
import com.expenseai.manager.domain.model.Income
import com.expenseai.manager.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class IncomeRepositoryImpl @Inject constructor(
    private val dao: IncomeDao
) : IncomeRepository {

    override fun getAllIncomes(): Flow<List<Income>> =
        dao.getAllIncomes().map { it.map(IncomeEntity::toDomain) }

    override fun getIncomesByCurrency(currency: String): Flow<List<Income>> =
        dao.getIncomesByCurrency(currency).map { it.map(IncomeEntity::toDomain) }

    override fun getIncomesByDateRange(start: Date, end: Date): Flow<List<Income>> =
        dao.getIncomesByDateRange(start.time, end.time).map { it.map(IncomeEntity::toDomain) }

    override suspend fun getIncomeById(id: Long): Income? =
        dao.getIncomeById(id)?.toDomain()

    override suspend fun insertIncome(income: Income): Long =
        dao.insertIncome(IncomeEntity.fromDomain(income))

    override suspend fun updateIncome(income: Income) =
        dao.updateIncome(IncomeEntity.fromDomain(income))

    override suspend fun deleteIncome(income: Income) =
        dao.deleteIncome(IncomeEntity.fromDomain(income))

    override fun getTotalIncomeByCurrency(currency: String, start: Date, end: Date): Flow<Double> =
        dao.getTotalIncomeByCurrency(currency, start.time, end.time)
}
