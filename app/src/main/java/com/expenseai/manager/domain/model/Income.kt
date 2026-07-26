package com.expenseai.manager.domain.model

import java.util.Date

data class Income(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val source: ExpenseCategory = ExpenseCategory.SALARY,
    val date: Date,
    val notes: String = "",
    val isRecurring: Boolean = false,
    val tags: List<String> = emptyList()
)
