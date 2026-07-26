package com.expenseai.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.PaymentMethod
import com.expenseai.manager.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val merchant: String,
    val date: Long,
    val paymentMethod: String,
    val notes: String,
    val tags: String,
    val receiptImagePath: String?,
    val isRecurring: Boolean,
    val taxAmount: Double,
    val type: String
) {
    fun toDomain() = Expense(
        id = id,
        title = title,
        description = description,
        amount = amount,
        currency = currency,
        category = ExpenseCategory.fromString(category),
        merchant = merchant,
        date = Date(date),
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        notes = notes,
        tags = if (tags.isBlank()) emptyList() else tags.split(","),
        receiptImagePath = receiptImagePath,
        isRecurring = isRecurring,
        taxAmount = taxAmount,
        type = TransactionType.valueOf(type)
    )

    companion object {
        fun fromDomain(expense: Expense) = ExpenseEntity(
            id = expense.id,
            title = expense.title,
            description = expense.description,
            amount = expense.amount,
            currency = expense.currency,
            category = expense.category.name,
            merchant = expense.merchant,
            date = expense.date.time,
            paymentMethod = expense.paymentMethod.name,
            notes = expense.notes,
            tags = expense.tags.joinToString(","),
            receiptImagePath = expense.receiptImagePath,
            isRecurring = expense.isRecurring,
            taxAmount = expense.taxAmount,
            type = expense.type.name
        )
    }
}
