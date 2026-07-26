package com.expenseai.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseai.manager.domain.model.Budget
import com.expenseai.manager.domain.model.ExpenseCategory
import java.util.Date

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    val category: String?,
    val month: Int,
    val year: Int,
    val alertThreshold: Double,
    val createdAt: Long
) {
    fun toDomain() = Budget(
        id = id,
        name = name,
        amount = amount,
        currency = currency,
        category = category?.let { ExpenseCategory.fromString(it) },
        month = month,
        year = year,
        alertThreshold = alertThreshold,
        createdAt = Date(createdAt)
    )

    companion object {
        fun fromDomain(budget: Budget) = BudgetEntity(
            id = budget.id,
            name = budget.name,
            amount = budget.amount,
            currency = budget.currency,
            category = budget.category?.name,
            month = budget.month,
            year = budget.year,
            alertThreshold = budget.alertThreshold,
            createdAt = budget.createdAt.time
        )
    }
}
