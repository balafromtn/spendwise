package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["budgetId"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val budgetId: String = UUID.randomUUID().toString(),
    val month: String,
    val category: String,
    val budgetAmount: Double,
    val spentSoFar: Double = 0.0,
    val syncStatus: String = "PENDING",
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val deleted: Boolean = false,
    val sheetRowId: Int? = null
)
