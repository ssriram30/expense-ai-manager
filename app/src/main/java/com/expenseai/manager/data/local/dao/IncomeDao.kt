package com.expenseai.manager.data.local.dao

import androidx.room.*
import com.expenseai.manager.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE currency = :currency ORDER BY date DESC")
    fun getIncomesByCurrency(currency: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getIncomesByDateRange(start: Long, end: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM incomes WHERE currency = :currency AND date BETWEEN :start AND :end")
    fun getTotalIncomeByCurrency(currency: String, start: Long, end: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)
}
