package com.expenseai.manager.domain.model

import java.util.Date

data class Transfer(
    val id: Long = 0,
    val title: String = "Money Transfer",
    val amount: Double,
    val fromCurrency: String,
    val toCurrency: String,
    val exchangeRate: Double,
    val convertedAmount: Double,
    val fee: Double = 0.0,
    val date: Date,
    val notes: String = "",
    val recipient: String = "",
    val transferMethod: String = "Bank Transfer"
)
