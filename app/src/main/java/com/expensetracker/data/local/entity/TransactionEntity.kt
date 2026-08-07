package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["transactionId"], unique = true), Index(value = ["syncStatus"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val type: String,
    val category: String,
    val amount: Double,
    val paymentMethod: String,
    val notes: String,
    val month: String,
    val weekNo: Int,
    val dateEpoch: Long = 0L,
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val deleted: Boolean = false,
    val sheetRowId: Int? = null
)
