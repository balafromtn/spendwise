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

    @Query("SELECT * FROM budgets WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSync(): List<BudgetEntity>

    @Query("UPDATE budgets SET syncStatus = 'SYNCED', sheetRowId = :sheetRowId WHERE id = :id")
    suspend fun markSynced(id: Long, sheetRowId: Int)

    @Query("UPDATE budgets SET spentSoFar = :spent WHERE month = :month AND category = :category")
    suspend fun updateSpent(month: String, category: String, spent: Double)

    @Query("DELETE FROM budgets WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSynced()
}
