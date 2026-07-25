package com.expensetracker.sync

import com.expensetracker.data.local.ExpenseDatabase
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.data.remote.SheetsSchema
import com.expensetracker.data.remote.SheetsService
import com.expensetracker.domain.usecase.AggregationUseCase
import com.expensetracker.domain.usecase.DateUtils

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
        for (t in pendingDelete) {
            database.transactionDao().deleteById(t.id)
        }
    }

    private suspend fun pushPendingBudgets(spreadsheetId: String) {
        val pending = database.budgetDao().getPendingSync()
        if (pending.isNotEmpty()) {
            sheetsService.appendBudgets(spreadsheetId, pending)
            for (b in pending) {
                database.budgetDao().markSynced(b.id, 0)
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
        aggregationUseCase.getMonthlySummary(month).collect { summary ->
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
            return@collect
        }
    }
}
