package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getByTransactionId(transactionId: String): TransactionEntity?

    @Query("SELECT transactionId FROM transactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTransactionIds(): List<String>

    @Query("SELECT * FROM transactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE syncStatus = 'PENDING_DELETE'")
    suspend fun getPendingDelete(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = 'SYNCED', sheetRowId = :sheetRowId WHERE id = :id")
    suspend fun markSynced(id: Long, sheetRowId: Int)

    @Query("UPDATE transactions SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSyncedNoRow(id: Long)

    @Query("UPDATE transactions SET syncStatus = 'SYNCED' WHERE transactionId = :transactionId")
    suspend fun markSyncedByTransactionId(transactionId: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income' AND month = :month")
    fun getTotalIncomeByMonth(month: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense' AND month = :month")
    fun getTotalExpenseByMonth(month: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND month = :month GROUP BY category")
    fun getCategoryTotals(type: String, month: String): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE month = :month ORDER BY createdAt DESC")
    fun getTransactionsByMonth(month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND month = :month ORDER BY createdAt DESC")
    fun getTransactionsByTypeAndMonth(type: String, month: String): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT MAX(amount) FROM transactions WHERE type = :type")
    fun getHighestAmount(type: String): Flow<Double?>

    @Query("SELECT MIN(amount) FROM transactions WHERE type = :type AND amount > 0")
    fun getLowestAmount(type: String): Flow<Double?>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:category IS NULL OR category = :category)
        AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
        ORDER BY createdAt DESC
    """)
    fun getFiltered(
        startDate: String?,
        endDate: String?,
        category: String?,
        paymentMethod: String?
    ): Flow<List<TransactionEntity>>

    @Query("SELECT AVG(amount) FROM transactions WHERE type = 'Expense' AND weekNo = :weekNo AND month = :month")
    fun getAverageWeeklySpend(weekNo: Int, month: String): Flow<Double?>

    @Query("DELETE FROM transactions WHERE syncStatus = 'SYNCED' AND sheetRowId IS NOT NULL")
    suspend fun deleteSynced()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
