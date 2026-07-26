package com.expenseai.manager.domain.model

import java.util.Date

data class RecurringExpense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory,
    val frequency: RecurringFrequency,
    val nextDueDate: Date,
    val reminderEnabled: Boolean = true,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val notes: String = ""
)

enum class RecurringFrequency(val displayName: String, val daysInterval: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BIWEEKLY("Bi-weekly", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}
