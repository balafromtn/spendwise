package com.expensetracker.sync

import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.usecase.DateUtils

object SheetRowCodec {

    private val dateUtils = DateUtils()

    fun transactionFromRow(row: List<Any>): TransactionEntity? {
        if (row.size < SheetsRef.TRANSACTION_COLUMNS) return null
        val uuid = row[0].toString().trim()
        if (uuid.isEmpty()) return null
        val created = row[7].toString().toLongOrNull() ?: 0L
        val updated = row[8].toString().toLongOrNull() ?: created
        val version = row[9].toString().toIntOrNull() ?: 1
        val deleted = row[10].toString().equals("TRUE", ignoreCase = true)
        val dateStr = row[1].toString()
        val parsedDate = dateUtils.parseSheetDate(dateStr) ?: run {
            android.util.Log.w("SheetRowCodec", "Skipping row with unparseable date: $dateStr (UUID=$uuid)")
            return null
        }
        val dateEpoch = dateUtils.toEpochMillis(parsedDate)
        return TransactionEntity(
            transactionId = uuid,
            date = dateStr,
            time = dateUtils.millisToTimeString(created),
            type = row[2].toString(),
            category = row[3].toString(),
            amount = row[4].toString().toDoubleOrNull() ?: 0.0,
            paymentMethod = row[5].toString(),
            notes = row[6].toString(),
            month = dateUtils.toMonthString(parsedDate),
            weekNo = dateUtils.toWeekNumber(parsedDate),
            dateEpoch = dateEpoch,
            syncStatus = "SYNCED",
            createdAt = if (created > 0) created else System.currentTimeMillis(),
            updatedAt = updated,
            version = version,
            deleted = deleted
        )
    }

    fun budgetFromRow(row: List<Any>): BudgetEntity? {
        if (row.size < SheetsRef.BUDGET_COLUMNS) return null
        val uuid = row[0].toString().trim()
        if (uuid.isEmpty()) return null
        val updated = row[5].toString().toLongOrNull() ?: 0L
        val version = row[6].toString().toIntOrNull() ?: 1
        val deleted = row[7].toString().equals("TRUE", ignoreCase = true)
        return BudgetEntity(
            budgetId = uuid,
            month = row[1].toString(),
            category = row[2].toString(),
            budgetAmount = row[3].toString().toDoubleOrNull() ?: 0.0,
            spentSoFar = row[4].toString().toDoubleOrNull() ?: 0.0,
            syncStatus = "SYNCED",
            updatedAt = updated,
            version = version,
            deleted = deleted
        )
    }

    fun categoryFromRow(row: List<Any>): CategoryEntity? {
        if (row.size < SheetsRef.CATEGORY_COLUMNS) return null
        val name = row[0].toString().trim()
        if (name.isEmpty()) return null
        val deleted = row[3].toString().equals("TRUE", ignoreCase = true)
        if (deleted) return null
        val isDefault = row[2].toString().equals("Default", ignoreCase = true)
        return CategoryEntity(
            name = name,
            type = row[1].toString(),
            isDefault = isDefault,
            isCustom = !isDefault
        )
    }
}

object SheetsRef {
    const val TRANSACTION_COLUMNS = 11
    const val BUDGET_COLUMNS = 8
    const val CATEGORY_COLUMNS = 4
}
