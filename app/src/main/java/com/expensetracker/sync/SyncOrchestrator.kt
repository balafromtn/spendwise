package com.expensetracker.sync

import android.content.Context
import android.provider.Settings
import com.expensetracker.BuildConfig
import com.expensetracker.data.local.ExpenseDatabase
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.data.remote.SheetsSchema
import com.expensetracker.data.remote.SheetsService
import com.expensetracker.di.SpreadsheetConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn

class SyncOrchestrator(
    private val database: ExpenseDatabase,
    private val sheetsService: SheetsService,
    private val context: Context
) {
    private val cacheRetentionDays = 30L
    private val cacheMaxRecords = 500

    suspend fun performSync() {
        val spreadsheetId = SpreadsheetConfig.getSpreadsheetId()
        if (spreadsheetId.isBlank() || spreadsheetId == "YOUR_SPREADSHEET_ID_HERE") return

        try {
            sheetsService.validateAndRepair(spreadsheetId)
            resetStaleStates()
            pushTransactions(spreadsheetId)
            pushBudgets(spreadsheetId)
            pushCategories(spreadsheetId)
            pushDeletes(spreadsheetId)
            pullFromSheets(spreadsheetId)
            cleanupCache()
            updateMetadata(spreadsheetId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun markPendingFailed() {
        database.transactionDao().markAllPendingFailed()
        database.budgetDao().markAllPendingFailed()
    }

    private suspend fun resetStaleStates() {
        database.transactionDao().resetSyncing()
        database.transactionDao().resetFailedToPending()
        database.budgetDao().resetSyncing()
        database.budgetDao().resetFailedToPending()
    }

    // ------------------------------------------------------------------
    // Uploads
    // ------------------------------------------------------------------

    private suspend fun pushTransactions(spreadsheetId: String) {
        val pending = database.transactionDao().getPendingSync()
        if (pending.isEmpty()) return

        val sheetRows = sheetsService.readAllTransactions(spreadsheetId)
        if (sheetRows.isEmpty()) return
        val header = sheetRows.first()
        if (header.isEmpty() || header.first() != "UUID") return

        val rowByUuid = mutableMapOf<String, Int>()
        sheetRows.drop(1).forEachIndexed { i, row ->
            if (row.size >= SheetsRef.TRANSACTION_COLUMNS) {
                val uuid = row[0].toString().trim()
                if (uuid.isNotEmpty()) rowByUuid[uuid] = i + 2
            }
        }

        val toAppend = mutableListOf<TransactionEntity>()
        val toUpdate = mutableListOf<Pair<Int, TransactionEntity>>()
        val toMarkSynced = mutableListOf<Long>()

        for (t in pending) {
            val rowIndex = rowByUuid[t.transactionId]
            if (rowIndex == null) {
                toAppend.add(t)
                toMarkSynced.add(t.id)
            } else {
                val remote = SheetRowCodec.transactionFromRow(sheetRows[rowIndex - 1])
                val remoteWins = remote != null &&
                    SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, t.updatedAt, t.version)
                if (remoteWins) {
                    database.transactionDao().update(remote.copy(id = t.id))
                    database.transactionDao().markSynced(t.id)
                } else {
                    toUpdate.add(rowIndex to t)
                    toMarkSynced.add(t.id)
                }
            }
        }

        if (toAppend.isNotEmpty()) {
            sheetsService.appendTransactions(spreadsheetId, toAppend)
        }
        for ((rowIndex, t) in toUpdate) {
            sheetsService.updateTransactionRow(spreadsheetId, rowIndex, t)
        }
        for (id in toMarkSynced) {
            database.transactionDao().markSynced(id)
        }
    }

    private suspend fun pushBudgets(spreadsheetId: String) {
        val pending = database.budgetDao().getPendingSync()
        if (pending.isEmpty()) return

        val sheetRows = sheetsService.readBudgets(spreadsheetId)
        if (sheetRows.isEmpty()) return
        val header = sheetRows.first()
        if (header.isEmpty() || header.first() != "UUID") return

        val rowByUuid = mutableMapOf<String, Int>()
        sheetRows.drop(1).forEachIndexed { i, row ->
            if (row.size >= SheetsRef.BUDGET_COLUMNS) {
                val uuid = row[0].toString().trim()
                if (uuid.isNotEmpty()) rowByUuid[uuid] = i + 2
            }
        }

        val toAppend = mutableListOf<com.expensetracker.data.local.entity.BudgetEntity>()
        val toUpdate = mutableListOf<Pair<Int, com.expensetracker.data.local.entity.BudgetEntity>>()
        val toMarkSynced = mutableListOf<Long>()

        for (b in pending) {
            val rowIndex = rowByUuid[b.budgetId]
            if (rowIndex == null) {
                toAppend.add(b)
                toMarkSynced.add(b.id)
            } else {
                val remote = SheetRowCodec.budgetFromRow(sheetRows[rowIndex - 1])
                val remoteWins = remote != null &&
                    SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, b.updatedAt, b.version)
                if (remoteWins) {
                    database.budgetDao().update(remote.copy(id = b.id))
                    database.budgetDao().markSynced(b.id)
                } else {
                    toUpdate.add(rowIndex to b)
                    toMarkSynced.add(b.id)
                }
            }
        }

        if (toAppend.isNotEmpty()) {
            sheetsService.appendBudgets(spreadsheetId, toAppend)
        }
        for ((rowIndex, b) in toUpdate) {
            sheetsService.updateBudgetRow(spreadsheetId, rowIndex, b)
        }
        for (id in toMarkSynced) {
            database.budgetDao().markSynced(id)
        }
    }

    private suspend fun pushCategories(spreadsheetId: String) {
        val categories = database.categoryDao().getAllOnce()
        sheetsService.writeCategories(spreadsheetId, categories)
    }

    private suspend fun pushDeletes(spreadsheetId: String) {
        val deletedTx = database.transactionDao().getDeleted()
        val deletedBudgets = database.budgetDao().getDeleted()
        if (deletedTx.isEmpty() && deletedBudgets.isEmpty()) return

        if (deletedTx.isNotEmpty()) {
            val deletedUuids = deletedTx.map { it.transactionId }.toSet()
            val allRows = sheetsService.readAllTransactions(spreadsheetId)
            if (allRows.isNotEmpty()) {
                val header = allRows.first()
                if (header.isNotEmpty() && header.first() == "UUID") {
                    val rowIndicesToDelete = mutableListOf<Int>()
                    allRows.drop(1).forEachIndexed { index, row ->
                        val uuid = if (row.size >= SheetsRef.TRANSACTION_COLUMNS) row[0].toString().trim() else ""
                        if (uuid in deletedUuids) {
                            rowIndicesToDelete.add(index + 2) // +1 for header, +1 for 1-based index
                        }
                    }
                    sheetsService.deleteRows(spreadsheetId, com.expensetracker.data.remote.SheetsSchema.TRANSACTIONS_SHEET, rowIndicesToDelete)
                }
            }
            for (t in deletedTx) {
                database.transactionDao().deleteById(t.id)
            }
        }

        if (deletedBudgets.isNotEmpty()) {
            val deletedUuids = deletedBudgets.map { it.budgetId }.toSet()
            val allRows = sheetsService.readBudgets(spreadsheetId)
            if (allRows.isNotEmpty()) {
                val header = allRows.first()
                if (header.isNotEmpty() && header.first() == "UUID") {
                    val rowIndicesToDelete = mutableListOf<Int>()
                    allRows.drop(1).forEachIndexed { index, row ->
                        val uuid = if (row.size >= SheetsRef.BUDGET_COLUMNS) row[0].toString().trim() else ""
                        if (uuid in deletedUuids) {
                            rowIndicesToDelete.add(index + 2)
                        }
                    }
                    sheetsService.deleteRows(spreadsheetId, com.expensetracker.data.remote.SheetsSchema.BUDGETS_SHEET, rowIndicesToDelete)
                }
            }
            for (b in deletedBudgets) {
                database.budgetDao().deleteById(b.id)
            }
        }
    }

    // ------------------------------------------------------------------
    // Downloads
    // ------------------------------------------------------------------

    private suspend fun pullFromSheets(spreadsheetId: String) {
        pullTransactions(spreadsheetId)
        pullBudgets(spreadsheetId)
        pullCategories(spreadsheetId)
    }

    private suspend fun pullTransactions(spreadsheetId: String) {
        val sheetRows = sheetsService.readAllTransactions(spreadsheetId)
        if (sheetRows.isEmpty()) return
        val header = sheetRows.first()
        if (header.isEmpty() || header.first() != "UUID") return

        for (row in sheetRows.drop(1)) {
            val remote = SheetRowCodec.transactionFromRow(row) ?: continue
            val local = database.transactionDao().getByTransactionId(remote.transactionId)
            if (local == null) {
                if (!remote.deleted) {
                    database.transactionDao().insert(remote)
                }
                continue
            }
            if (remote.deleted) {
                if (SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, local.updatedAt, local.version)) {
                    database.transactionDao().deleteById(local.id)
                }
                continue
            }
            if (SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, local.updatedAt, local.version)) {
                database.transactionDao().update(remote.copy(id = local.id))
            }
        }
    }

    private suspend fun pullBudgets(spreadsheetId: String) {
        val sheetRows = sheetsService.readBudgets(spreadsheetId)
        if (sheetRows.isEmpty()) return
        val header = sheetRows.first()
        if (header.isEmpty() || header.first() != "UUID") return

        for (row in sheetRows.drop(1)) {
            val remote = SheetRowCodec.budgetFromRow(row) ?: continue
            val local = database.budgetDao().getByBudgetId(remote.budgetId)
            if (local == null) {
                if (!remote.deleted) {
                    database.budgetDao().insert(remote)
                }
                continue
            }
            if (remote.deleted) {
                if (SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, local.updatedAt, local.version)) {
                    database.budgetDao().deleteById(local.id)
                }
                continue
            }
            if (SyncUtils.isRemoteNewer(remote.updatedAt, remote.version, local.updatedAt, local.version)) {
                database.budgetDao().update(remote.copy(id = local.id))
            }
        }
    }

    private suspend fun pullCategories(spreadsheetId: String) {
        val sheetRows = sheetsService.readCategories(spreadsheetId)
        if (sheetRows.isEmpty()) return
        val header = sheetRows.first()
        if (header.isEmpty() || header.first() != "Category") return

        val existing = database.categoryDao().getAllOnce().map { it.name to it.type }.toSet()
        val toInsert = sheetRows.drop(1).mapNotNull { SheetRowCodec.categoryFromRow(it) }
            .filter { (it.name to it.type) !in existing }
        if (toInsert.isNotEmpty()) {
            database.categoryDao().insertAll(toInsert)
        }
    }

    // ------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------

    private suspend fun cleanupCache() {
        val cutoff = System.currentTimeMillis() - cacheRetentionDays * 24 * 60 * 60 * 1000
        database.transactionDao().deleteSyncedCacheOlderThan(cutoff)
        database.transactionDao().deleteSyncedCacheExcess(cacheMaxRecords)
    }

    private suspend fun updateMetadata(spreadsheetId: String) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val now = System.currentTimeMillis().toString()
        val values = linkedMapOf(
            SheetsSchema.KEY_SCHEMA_VERSION to SheetsSchema.SCHEMA_VERSION.toString(),
            SheetsSchema.KEY_LAST_SYNC to now,
            SheetsSchema.KEY_APP_VERSION to BuildConfig.VERSION_NAME,
            SheetsSchema.KEY_USER_EMAIL to (account?.email ?: ""),
            SheetsSchema.KEY_DEVICE_ID to (Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""),
            SheetsSchema.KEY_LAST_VALIDATION to now
        )
        sheetsService.writeMetadata(spreadsheetId, values)
    }
}
