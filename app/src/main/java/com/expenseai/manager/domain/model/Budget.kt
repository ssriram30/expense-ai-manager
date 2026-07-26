package com.expenseai.manager.domain.model

import java.util.Date

data class Budget(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory?,
    val month: Int,
    val year: Int,
    val alertThreshold: Double = 0.80,
    val createdAt: Date = Date()
)

data class BudgetStatus(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentageUsed: Double,
    val isOverBudget: Boolean,
    val isNearLimit: Boolean
)
