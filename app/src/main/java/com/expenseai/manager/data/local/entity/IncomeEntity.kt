package com.expenseai.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.Income
import java.util.Date

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val source: String,
    val date: Long,
    val notes: String,
    val isRecurring: Boolean,
    val tags: String
) {
    fun toDomain() = Income(
        id = id,
        title = title,
        amount = amount,
        currency = currency,
        source = ExpenseCategory.fromString(source),
        date = Date(date),
        notes = notes,
        isRecurring = isRecurring,
        tags = if (tags.isBlank()) emptyList() else tags.split(",")
    )

    companion object {
        fun fromDomain(income: Income) = IncomeEntity(
            id = income.id,
            title = income.title,
            amount = income.amount,
            currency = income.currency,
            source = income.source.name,
            date = income.date.time,
            notes = income.notes,
            isRecurring = income.isRecurring,
            tags = income.tags.joinToString(",")
        )
    }
}
