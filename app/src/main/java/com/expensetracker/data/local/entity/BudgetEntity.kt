package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String,
    val category: String,
    val budgetAmount: Double,
    val spentSoFar: Double = 0.0,
    val syncStatus: String = "PENDING",
    val sheetRowId: Int? = null
)
