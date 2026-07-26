package com.expenseai.manager.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Expense(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory,
    val merchant: String = "",
    val date: Date,
    val paymentMethod: PaymentMethod,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val receiptImagePath: String? = null,
    val isRecurring: Boolean = false,
    val taxAmount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE
) : Parcelable

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD_DINING("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    HEALTH_FITNESS("Health & Fitness", "💊"),
    ENTERTAINMENT("Entertainment", "🎬"),
    BILLS_UTILITIES("Bills & Utilities", "💡"),
    EDUCATION("Education", "📚"),
    TRAVEL("Travel", "✈️"),
    GROCERIES("Groceries", "🛒"),
    TRANSFER("Transfer", "💸"),
    SALARY("Salary", "💰"),
    FREELANCE("Freelance", "💻"),
    INVESTMENT("Investment", "📈"),
    RENTAL("Rental", "🏠"),
    OTHER("Other", "📦");

    companion object {
        fun fromString(value: String): ExpenseCategory =
            values().find { it.name == value } ?: OTHER

        fun expenseCategories() = listOf(
            FOOD_DINING, TRANSPORT, SHOPPING, HEALTH_FITNESS, ENTERTAINMENT,
            BILLS_UTILITIES, EDUCATION, TRAVEL, GROCERIES, OTHER
        )

        fun incomeCategories() = listOf(SALARY, FREELANCE, INVESTMENT, RENTAL, OTHER)
    }
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    E_WALLET("E-Wallet"),
    UPI("UPI"),
    CRYPTO("Crypto"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): PaymentMethod =
            values().find { it.name == value } ?: CASH
    }
}

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}
