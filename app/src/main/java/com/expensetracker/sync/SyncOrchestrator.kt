package com.expensetracker.sync

import com.expensetracker.data.local.ExpenseDatabase
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.data.remote.SheetsSchema
import com.expensetracker.data.remote.SheetsService
import com.expensetracker.domain.usecase.AggregationUseCase
import com.expensetracker.domain.usecase.DateUtils
import kotlinx.coroutines.flow.first

class SyncOrchestrator(
    private val database: ExpenseDatabase,
    private val sheetsService: SheetsService,
    private val aggregationUseCase: AggregationUseCase
) {
    private val dateUtils = DateUtils()

    suspend fun performSync() {
        val spreadsheetId = com.expensetracker.di.SpreadsheetConfig.getSpreadsheetId()
        if (spreadsheetId.isBlank() || spreadsheetId == "YOUR_SPREADSHEET_ID_HERE") return

        try {
            sheetsService.ensureSheetsExist(spreadsheetId)
            pushPendingTransactions(spreadsheetId)
            pushPendingDeletes(spreadsheetId)
            pushPendingBudgets(spreadsheetId)
            pullFromSheets(spreadsheetId)
            recomputeAndPushSummary(spreadsheetId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun pushPendingTransactions(spreadsheetId: String) {
        val pending = database.transactionDao().getPendingSync()
        if (pending.isNotEmpty()) {
            sheetsService.appendTransactions(spreadsheetId, pending)
            for (t in pending) {
                database.transactionDao().markSyncedByTransactionId(t.transactionId)
            }
        }
    }

    private suspend fun pushPendingDeletes(spreadsheetId: String) {
        val pendingDelete = database.transactionDao().getPendingDelete()
        if (pendingDelete.isEmpty()) return

        val deletedIds = pendingDelete.map { it.transactionId }.toSet()
        val allRows = sheetsService.readAllTransactions(spreadsheetId)
        if (allRows.isEmpty()) return

        val header = allRows.first()
        if (header.isEmpty() || header.first() != "Date") return

        val filteredRows = listOf(header) + allRows.drop(1).filter { row ->
            val id = if (row.size >= 10) row[9].toString() else ""
            id !in deletedIds
        }
        sheetsService.writeTransactions(spreadsheetId, filteredRows)
        for (t in pendingDelete) {
            database.transactionDao().deleteById(t.id)
        }
    }

    private suspend fun pushPendingBudgets(spreadsheetId: String) {
        val pending = database.budgetDao().getPendingSync()
        if (pending.isNotEmpty()) {
            val startRow = sheetsService.appendBudgets(spreadsheetId, pending)
            if (startRow != null) {
                pending.forEachIndexed { index, b ->
                    database.budgetDao().markSynced(b.id, startRow + index)
                }
            }
        }
    }

    private suspend fun pullFromSheets(spreadsheetId: String) {
        val sheetTransactions = sheetsService.readAllTransactions(spreadsheetId)
        if (sheetTransactions.isEmpty()) return

        val header = sheetTransactions.first()
        if (header.isEmpty() || header.first() != "Date") return

        val dataRows = sheetTransactions.drop(1)
        val entities = dataRows.mapNotNull { row ->
            try {
                if (row.size >= 9) {
                    val txnId = if (row.size >= 10) row[9].toString() else ""
                    if (txnId.isBlank()) null else TransactionEntity(
                        transactionId = txnId,
                        date = row[0].toString(),
                        time = row[1].toString(),
                        type = row[2].toString(),
                        category = row[3].toString(),
                        amount = row[4].toString().toDoubleOrNull() ?: 0.0,
                        paymentMethod = row[5].toString(),
                        notes = row[6].toString(),
                        month = row[7].toString(),
                        weekNo = row[8].toString().toIntOrNull() ?: 0,
                        syncStatus = "SYNCED"
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        for (entity in entities) {
            val existing = database.transactionDao().getByTransactionId(entity.transactionId)
            if (existing == null) {
                database.transactionDao().insert(entity)
            }
        }
    }

    private suspend fun recomputeAndPushSummary(spreadsheetId: String) {
        val month = dateUtils.currentMonthString()
        val summary = aggregationUseCase.getMonthlySummary(month).first()

        val incomeBreakdown = summary.categoryBreakdown
            .filter { it.type == "Income" }
            .map { it.category to it.amount }
        val expenseBreakdown = summary.categoryBreakdown
            .filter { it.type == "Expense" }
            .map { it.category to it.amount }

        val rows = SheetsSchema.summaryRows(
            totalIncome = summary.totalIncome,
            totalExpense = summary.totalExpense,
            netSavings = summary.netSavings,
            totalTransactions = summary.transactionCount,
            highestIncome = summary.highestIncome,
            lowestIncome = summary.lowestIncome,
            highestExpense = summary.highestExpense,
            lowestExpense = summary.lowestExpense,
            incomeBreakdown = incomeBreakdown,
            expenseBreakdown = expenseBreakdown
        )
        sheetsService.rewriteSummary(spreadsheetId, rows)
    }
}
