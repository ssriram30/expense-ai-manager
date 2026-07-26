package com.expenseai.manager.domain.repository

import com.expenseai.manager.domain.model.Transfer
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransferRepository {
    fun getAllTransfers(): Flow<List<Transfer>>
    fun getTransfersByDateRange(start: Date, end: Date): Flow<List<Transfer>>
    suspend fun getTransferById(id: Long): Transfer?
    suspend fun insertTransfer(transfer: Transfer): Long
    suspend fun updateTransfer(transfer: Transfer)
    suspend fun deleteTransfer(transfer: Transfer)
}
