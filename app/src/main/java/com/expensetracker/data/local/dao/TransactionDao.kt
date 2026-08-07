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

    @Query("UPDATE transactions SET deleted = 1, updatedAt = :now, version = version + 1 WHERE id = :id")
    suspend fun markDeleted(id: Long, now: Long)

    @Query("""
        UPDATE transactions 
        SET category = :newName, syncStatus = 'PENDING', 
            updatedAt = :now, version = version + 1 
        WHERE category = :oldName AND type = :type AND deleted = 0
    """)
    suspend fun renameTransactions(oldName: String, newName: String, type: String, now: Long)

    @Query("SELECT * FROM transactions WHERE deleted = 0 ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE deleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getByTransactionId(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE syncStatus = 'PENDING' AND deleted = 0")
    suspend fun getPendingSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE syncStatus = 'FAILED' AND deleted = 0")
    suspend fun getFailedSync(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = 'PENDING' WHERE syncStatus = 'FAILED' AND deleted = 0")
    suspend fun resetFailedToPending()

    @Query("UPDATE transactions SET syncStatus = 'SYNCING' WHERE syncStatus IN ('PENDING', 'FAILED') AND deleted = 0")
    suspend fun markSyncing()

    @Query("UPDATE transactions SET syncStatus = 'PENDING' WHERE syncStatus = 'SYNCING'")
    suspend fun resetSyncing()

    @Query("UPDATE transactions SET syncStatus = 'FAILED' WHERE syncStatus IN ('PENDING', 'SYNCING')")
    suspend fun markAllPendingFailed()

    @Query("SELECT * FROM transactions WHERE deleted = 1")
    suspend fun getDeleted(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE transactions SET syncStatus = 'SYNCED' WHERE transactionId = :transactionId")
    suspend fun markSyncedByTransactionId(transactionId: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income' AND month = :month AND deleted = 0")
    fun getTotalIncomeByMonth(month: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense' AND month = :month AND deleted = 0")
    fun getTotalExpenseByMonth(month: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND month = :month AND deleted = 0 GROUP BY category")
    fun getCategoryTotals(type: String, month: String): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE month = :month AND deleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByMonth(month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND month = :month AND deleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByTypeAndMonth(type: String, month: String): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT MAX(amount) FROM transactions WHERE type = :type AND month = :month AND deleted = 0")
    fun getHighestAmount(type: String, month: String): Flow<Double?>

    @Query("SELECT MIN(amount) FROM transactions WHERE type = :type AND amount > 0 AND month = :month AND deleted = 0")
    fun getLowestAmount(type: String, month: String): Flow<Double?>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:startEpoch IS NULL OR dateEpoch >= :startEpoch)
        AND (:endEpoch IS NULL OR dateEpoch <= :endEpoch)
        AND (:category IS NULL OR category = :category)
        AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
        AND deleted = 0
        ORDER BY dateEpoch DESC
    """)
    fun getFiltered(
        startEpoch: Long?,
        endEpoch: Long?,
        category: String?,
        paymentMethod: String?
    ): Flow<List<TransactionEntity>>

    // Smart cache cleanup: synced rows older than the cutoff are removed first.
    @Query("DELETE FROM transactions WHERE syncStatus = 'SYNCED' AND deleted = 0 AND createdAt < :cutoff")
    suspend fun deleteSyncedCacheOlderThan(cutoff: Long): Int

    // Keep only the newest :keep synced rows, removing the rest.
    @Query("""
        DELETE FROM transactions 
        WHERE syncStatus = 'SYNCED' AND deleted = 0 
        AND id NOT IN (
            SELECT id FROM transactions 
            WHERE syncStatus = 'SYNCED' AND deleted = 0 
            ORDER BY createdAt DESC LIMIT :keep
        )
    """)
    suspend fun deleteSyncedCacheExcess(keep: Int): Int
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
