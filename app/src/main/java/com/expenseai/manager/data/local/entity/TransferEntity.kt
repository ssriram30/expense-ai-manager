package com.expenseai.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseai.manager.domain.model.Transfer
import java.util.Date

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val fromCurrency: String,
    val toCurrency: String,
    val exchangeRate: Double,
    val convertedAmount: Double,
    val fee: Double,
    val date: Long,
    val notes: String,
    val recipient: String,
    val transferMethod: String
) {
    fun toDomain() = Transfer(
        id = id,
        title = title,
        amount = amount,
        fromCurrency = fromCurrency,
        toCurrency = toCurrency,
        exchangeRate = exchangeRate,
        convertedAmount = convertedAmount,
        fee = fee,
        date = Date(date),
        notes = notes,
        recipient = recipient,
        transferMethod = transferMethod
    )

    companion object {
        fun fromDomain(transfer: Transfer) = TransferEntity(
            id = transfer.id,
            title = transfer.title,
            amount = transfer.amount,
            fromCurrency = transfer.fromCurrency,
            toCurrency = transfer.toCurrency,
            exchangeRate = transfer.exchangeRate,
            convertedAmount = transfer.convertedAmount,
            fee = transfer.fee,
            date = transfer.date.time,
            notes = transfer.notes,
            recipient = transfer.recipient,
            transferMethod = transfer.transferMethod
        )
    }
}
