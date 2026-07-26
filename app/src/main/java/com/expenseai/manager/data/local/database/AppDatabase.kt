package com.expenseai.manager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.expenseai.manager.data.local.dao.*
import com.expenseai.manager.data.local.entity.*

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class,
        IncomeEntity::class,
        TransferEntity::class,
        RecurringExpenseEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun incomeDao(): IncomeDao
    abstract fun transferDao(): TransferDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_ai_manager.db"
    }
}
