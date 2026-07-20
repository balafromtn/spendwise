package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val type: String,
    val category: String,
    val amount: Double,
    val paymentMethod: String,
    val notes: String,
    val month: String,
    val weekNo: Int,
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val sheetRowId: Int? = null
)
