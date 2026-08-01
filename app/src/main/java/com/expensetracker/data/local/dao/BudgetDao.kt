package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsByMonth(month: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month AND category = :category")
    suspend fun getBudget(month: String, category: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE budgetId = :budgetId LIMIT 1")
    suspend fun getByBudgetId(budgetId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE syncStatus = 'PENDING' AND deleted = 0")
    suspend fun getPendingSync(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE deleted = 1")
    suspend fun getDeleted(): List<BudgetEntity>

    @Query("UPDATE budgets SET syncStatus = 'PENDING' WHERE syncStatus = 'FAILED' AND deleted = 0")
    suspend fun resetFailedToPending()

    @Query("UPDATE budgets SET syncStatus = 'SYNCING' WHERE syncStatus IN ('PENDING', 'FAILED') AND deleted = 0")
    suspend fun markSyncing()

    @Query("UPDATE budgets SET syncStatus = 'PENDING' WHERE syncStatus = 'SYNCING'")
    suspend fun resetSyncing()

    @Query("UPDATE budgets SET syncStatus = 'FAILED' WHERE syncStatus IN ('PENDING', 'SYNCING')")
    suspend fun markAllPendingFailed()

    @Query("UPDATE budgets SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE budgets SET deleted = 1, updatedAt = :now, version = version + 1 WHERE id = :id")
    suspend fun markDeleted(id: Long, now: Long)

    @Query("UPDATE budgets SET spentSoFar = :spent WHERE month = :month AND category = :category")
    suspend fun updateSpent(month: String, category: String, spent: Double)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
