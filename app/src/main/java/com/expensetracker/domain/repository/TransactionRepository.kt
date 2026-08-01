package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>>
    fun getFiltered(startDate: String?, endDate: String?, category: String?, paymentMethod: String?): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
}
