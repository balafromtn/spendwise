package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.mapper.toDomain
import com.expensetracker.data.mapper.toEntity
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return dao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFiltered(
        startDate: String?,
        endDate: String?,
        category: String?,
        paymentMethod: String?
    ): Flow<List<Transaction>> {
        val dateUtils = com.expensetracker.domain.usecase.DateUtils()
        val startEpoch = startDate?.let { dateUtils.parseSheetDate(it)?.let { d -> dateUtils.toEpochMillis(d) } }
        val endEpoch = endDate?.let { dateUtils.parseSheetDate(it)?.let { d ->
            dateUtils.toEpochMillis(d) + (24 * 60 * 60 * 1000 - 1) // end of that day
        } }
        return dao.getFiltered(startEpoch, endEpoch, category, paymentMethod).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Transaction? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insert(transaction: Transaction) {
        val dateUtils = com.expensetracker.domain.usecase.DateUtils()
        val dateEpoch = dateUtils.parseSheetDate(transaction.date)?.let { dateUtils.toEpochMillis(it) } ?: System.currentTimeMillis()
        dao.insert(transaction.toEntity().copy(dateEpoch = dateEpoch))
    }

    override suspend fun update(transaction: Transaction) {
        val existing = dao.getById(transaction.id)
        if (existing != null) {
            val dateUtils = com.expensetracker.domain.usecase.DateUtils()
            val dateEpoch = dateUtils.parseSheetDate(transaction.date)?.let { dateUtils.toEpochMillis(it) } ?: existing.dateEpoch
            val updated = transaction.toEntity().copy(
                transactionId = existing.transactionId,
                dateEpoch = dateEpoch,
                version = existing.version + 1,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
            dao.update(updated)
        }
    }

    override suspend fun delete(transaction: Transaction) {
        dao.markDeleted(transaction.id, System.currentTimeMillis())
    }
}
