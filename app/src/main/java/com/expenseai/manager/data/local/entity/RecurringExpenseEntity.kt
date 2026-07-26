package com.expenseai.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.PaymentMethod
import com.expenseai.manager.domain.model.RecurringExpense
import com.expenseai.manager.domain.model.RecurringFrequency
import java.util.Date

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val frequency: String,
    val nextDueDate: Long,
    val reminderEnabled: Boolean,
    val paymentMethod: String,
    val notes: String
) {
    fun toDomain() = RecurringExpense(
        id = id,
        title = title,
        amount = amount,
        currency = currency,
        category = ExpenseCategory.fromString(category),
        frequency = RecurringFrequency.valueOf(frequency),
        nextDueDate = Date(nextDueDate),
        reminderEnabled = reminderEnabled,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        notes = notes
    )

    companion object {
        fun fromDomain(expense: RecurringExpense) = RecurringExpenseEntity(
            id = expense.id,
            title = expense.title,
            amount = expense.amount,
            currency = expense.currency,
            category = expense.category.name,
            frequency = expense.frequency.name,
            nextDueDate = expense.nextDueDate.time,
            reminderEnabled = expense.reminderEnabled,
            paymentMethod = expense.paymentMethod.name,
            notes = expense.notes
        )
    }
}
