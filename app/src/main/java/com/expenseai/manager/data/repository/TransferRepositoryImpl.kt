package com.expenseai.manager.data.repository

import com.expenseai.manager.data.local.dao.TransferDao
import com.expenseai.manager.data.local.entity.TransferEntity
import com.expenseai.manager.domain.model.Transfer
import com.expenseai.manager.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val dao: TransferDao
) : TransferRepository {

    override fun getAllTransfers(): Flow<List<Transfer>> =
        dao.getAllTransfers().map { it.map(TransferEntity::toDomain) }

    override fun getTransfersByDateRange(start: Date, end: Date): Flow<List<Transfer>> =
        dao.getTransfersByDateRange(start.time, end.time).map { it.map(TransferEntity::toDomain) }

    override suspend fun getTransferById(id: Long): Transfer? =
        dao.getTransferById(id)?.toDomain()

    override suspend fun insertTransfer(transfer: Transfer): Long =
        dao.insertTransfer(TransferEntity.fromDomain(transfer))

    override suspend fun updateTransfer(transfer: Transfer) =
        dao.updateTransfer(TransferEntity.fromDomain(transfer))

    override suspend fun deleteTransfer(transfer: Transfer) =
        dao.deleteTransfer(TransferEntity.fromDomain(transfer))
}
