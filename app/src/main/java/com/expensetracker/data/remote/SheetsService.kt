package com.expensetracker.data.remote

import android.util.Log
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.usecase.DateUtils
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.min

class SheetsService(private val tokenProvider: TokenProvider) {

    private val dateUtils = DateUtils()

    private fun getService(): Sheets? = tokenProvider.getSheetsService()

    /**
     * Ensures all required sheets/headers exist, migrates legacy v1 spreadsheets,
     * and restores metadata. Never deletes valid transaction data.
     */
    suspend fun validateAndRepair(spreadsheetId: String) = withContext(Dispatchers.IO) {
        val service = getService() ?: throw IllegalStateException("Not authenticated")

        val existingTitles = sheetTitles(service, spreadsheetId)
        val sheetsToCreate = listOf(
            SheetsSchema.TRANSACTIONS_SHEET,
            SheetsSchema.BUDGETS_SHEET,
            SheetsSchema.CATEGORIES_SHEET,
            SheetsSchema.METADATA_SHEET
        ).filter { it !in existingTitles }

        if (sheetsToCreate.isNotEmpty()) {
            val requests = sheetsToCreate.map { name ->
                Request().setAddSheet(
                    AddSheetRequest().setProperties(
                        com.google.api.services.sheets.v4.model.SheetProperties().setTitle(name)
                    )
                )
            }
            service.spreadsheets()
                .batchUpdate(spreadsheetId, BatchUpdateSpreadsheetRequest().setRequests(requests))
                .execute()
        }

        if (SheetsSchema.TRANSACTIONS_SHEET in existingTitles) {
            migrateLegacyTransactions(service, spreadsheetId)
        }

        ensureHeaders(
            service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET,
            SheetsSchema.TRANSACTIONS_HEADERS, SheetsSchema.TRANSACTIONS_SHEET !in existingTitles
        )
        ensureHeaders(
            service, spreadsheetId, SheetsSchema.BUDGETS_SHEET,
            SheetsSchema.BUDGETS_HEADERS, SheetsSchema.BUDGETS_SHEET !in existingTitles
        )
        ensureHeaders(
            service, spreadsheetId, SheetsSchema.CATEGORIES_SHEET,
            SheetsSchema.CATEGORIES_HEADERS, SheetsSchema.CATEGORIES_SHEET !in existingTitles
        )
        ensureHeaders(
            service, spreadsheetId, SheetsSchema.METADATA_SHEET,
            SheetsSchema.METADATA_HEADERS, SheetsSchema.METADATA_SHEET !in existingTitles
        )
    }

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    suspend fun readAllTransactions(spreadsheetId: String): List<List<Any>> = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext emptyList()
        val rowCount = sheetRowCount(service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET)
        val lastCol = columnLetter(SheetsSchema.TRANSACTIONS_HEADERS.size)
        val pageSize = 500
        val all = mutableListOf<List<Any>>()
        var start = 1
        while (start <= rowCount) {
            val end = min(start + pageSize - 1, rowCount)
            val page = readRange(service, spreadsheetId, "${SheetsSchema.TRANSACTIONS_SHEET}!A$start:$lastCol$end")
                ?: break
            all.addAll(page)
            if (page.size < pageSize) break
            start = end + 1
        }
        all
    }

    suspend fun readBudgets(spreadsheetId: String): List<List<Any>> = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext emptyList()
        val rowCount = sheetRowCount(service, spreadsheetId, SheetsSchema.BUDGETS_SHEET)
        val lastCol = columnLetter(SheetsSchema.BUDGETS_HEADERS.size)
        if (rowCount <= 0) return@withContext emptyList()
        readRange(service, spreadsheetId, "${SheetsSchema.BUDGETS_SHEET}!A1:$lastCol$rowCount") ?: emptyList()
    }

    suspend fun readCategories(spreadsheetId: String): List<List<Any>> = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext emptyList()
        val rowCount = sheetRowCount(service, spreadsheetId, SheetsSchema.CATEGORIES_SHEET)
        val lastCol = columnLetter(SheetsSchema.CATEGORIES_HEADERS.size)
        if (rowCount <= 0) return@withContext emptyList()
        readRange(service, spreadsheetId, "${SheetsSchema.CATEGORIES_SHEET}!A1:$lastCol$rowCount") ?: emptyList()
    }

    suspend fun readMetadata(spreadsheetId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext emptyMap()
        val rows = readRange(service, spreadsheetId, "${SheetsSchema.METADATA_SHEET}!A1:B50") ?: emptyList()
        SheetsSchema.parseMetadata(rows)
    }

    // ------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------

    suspend fun appendTransactions(
        spreadsheetId: String,
        transactions: List<TransactionEntity>
    ): List<Int>? = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext null
        if (transactions.isEmpty()) return@withContext emptyList()
        appendRows(service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET, transactions.map { SheetsSchema.transactionRow(it) })
    }

    suspend fun appendBudgets(
        spreadsheetId: String,
        budgets: List<BudgetEntity>
    ): List<Int>? = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext null
        if (budgets.isEmpty()) return@withContext emptyList()
        appendRows(service, spreadsheetId, SheetsSchema.BUDGETS_SHEET, budgets.map { SheetsSchema.budgetRow(it) })
    }

    suspend fun updateTransactionRow(
        spreadsheetId: String,
        rowIndex: Int,
        transaction: TransactionEntity
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        updateRow(service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET, rowIndex, SheetsSchema.transactionRow(transaction))
    }

    suspend fun updateBudgetRow(
        spreadsheetId: String,
        rowIndex: Int,
        budget: BudgetEntity
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        updateRow(service, spreadsheetId, SheetsSchema.BUDGETS_SHEET, rowIndex, SheetsSchema.budgetRow(budget))
    }

    /**
     * Batch-updates multiple rows in a single HTTP request.
     * Each entry is (1-based rowIndex, row values).
     * This avoids per-row rate limits on the Google Sheets API.
     */
    suspend fun batchUpdateRows(
        spreadsheetId: String,
        sheetName: String,
        updates: List<Pair<Int, List<Any>>>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        if (updates.isEmpty()) return@withContext

        val colCount = when (sheetName) {
            SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS.size
            SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS.size
            else -> updates.first().second.size
        }
        val lastCol = columnLetter(colCount)

        val data = updates.map { (rowIndex, values) ->
            ValueRange()
                .setRange("$sheetName!A$rowIndex:$lastCol$rowIndex")
                .setValues(listOf(values))
        }

        val batchBody = com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest()
            .setValueInputOption("RAW")
            .setData(data)

        service.spreadsheets().values()
            .batchUpdate(spreadsheetId, batchBody)
            .execute()
    }

    /** Deletes specific 1-based row indices from the sheet efficiently. */
    suspend fun deleteRows(
        spreadsheetId: String,
        sheetName: String,
        rowIndices: List<Int>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        if (rowIndices.isEmpty()) return@withContext
        val sheetId = getSheetId(service, spreadsheetId, sheetName) ?: return@withContext

        // Sort descending so deleting higher indices doesn't shift lower indices
        val sorted0BasedIndices = rowIndices.map { it - 1 }.sortedDescending()

        val requests = sorted0BasedIndices.map { index ->
            Request().setDeleteDimension(
                com.google.api.services.sheets.v4.model.DeleteDimensionRequest().setRange(
                    com.google.api.services.sheets.v4.model.DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setStartIndex(index)
                        .setEndIndex(index + 1)
                )
            )
        }

        service.spreadsheets()
            .batchUpdate(spreadsheetId, BatchUpdateSpreadsheetRequest().setRequests(requests))
            .execute()
    }

    /** Clears and rewrites the Categories sheet from local state (categories are few). */
    suspend fun writeCategories(
        spreadsheetId: String,
        categories: List<CategoryEntity>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        val rows = categories.map { SheetsSchema.categoryRow(it) }
        rewriteSheet(service, spreadsheetId, SheetsSchema.CATEGORIES_SHEET, SheetsSchema.CATEGORIES_HEADERS, rows)
    }

    suspend fun writeMetadata(
        spreadsheetId: String,
        values: Map<String, String>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        val lastCol = columnLetter(SheetsSchema.METADATA_HEADERS.size)
        service.spreadsheets().values()
            .clear(spreadsheetId, "${SheetsSchema.METADATA_SHEET}!A1:${lastCol}100", ClearValuesRequest())
            .execute()
        val rows = SheetsSchema.metadataRows(values)
        service.spreadsheets().values()
            .update(
                spreadsheetId,
                "${SheetsSchema.METADATA_SHEET}!A1:$lastCol${rows.size}",
                ValueRange().setValues(rows)
            )
            .setValueInputOption("RAW")
            .execute()
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private suspend fun getSheetId(service: Sheets, spreadsheetId: String, sheetName: String): Int? {
        val spreadsheet = service.spreadsheets().get(spreadsheetId)
            .setFields("sheets.properties.title,sheets.properties.sheetId")
            .execute()
        return spreadsheet.sheets?.firstOrNull { it.properties?.title == sheetName }?.properties?.sheetId
    }

    private suspend fun sheetTitles(service: Sheets, spreadsheetId: String): List<String> {
        val spreadsheet = service.spreadsheets().get(spreadsheetId)
            .setFields("sheets.properties.title")
            .execute()
        return spreadsheet.sheets?.mapNotNull { it.properties?.title } ?: emptyList()
    }

    private suspend fun sheetRowCount(service: Sheets, spreadsheetId: String, sheetName: String): Int {
        val spreadsheet = service.spreadsheets().get(spreadsheetId)
            .setFields("sheets.properties.title,sheets.properties.gridProperties.rowCount")
            .execute()
        return spreadsheet.sheets
            ?.firstOrNull { it.properties?.title == sheetName }
            ?.properties?.gridProperties?.rowCount ?: 0
    }

    private suspend fun migrateLegacyTransactions(service: Sheets, spreadsheetId: String) {
        val legacyLastCol = columnLetter(SheetsSchema.LEGACY_TRANSACTIONS_HEADERS.size)
        val headerRange = "${SheetsSchema.TRANSACTIONS_SHEET}!A1:$legacyLastCol${1}"
        val header = readRange(service, spreadsheetId, headerRange)?.firstOrNull() ?: return
        if (header != SheetsSchema.LEGACY_TRANSACTIONS_HEADERS) return

        val rowCount = sheetRowCount(service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET)
        if (rowCount <= 1) return

        val legacyRows = readRange(service, spreadsheetId, "${SheetsSchema.TRANSACTIONS_SHEET}!A2:$legacyLastCol$rowCount") ?: emptyList()
        val migrated = legacyRows.mapNotNull { row ->
            if (row.size < 10) return@mapNotNull null
            val uuid = row[9].toString().ifBlank { UUID.randomUUID().toString() }
            val created = dateUtils.legacyDateTimeMillis(row[0].toString(), row[1].toString())
            listOf<Any>(
                uuid, row[0], row[2], row[3], row[4], row[5], row[6],
                created, created, 1, "FALSE"
            )
        }

        val lastCol = columnLetter(SheetsSchema.TRANSACTIONS_HEADERS.size)
        service.spreadsheets().values()
            .clear(spreadsheetId, "${SheetsSchema.TRANSACTIONS_SHEET}!A1:$lastCol$rowCount", ClearValuesRequest())
            .execute()
        writeHeaderRow(service, spreadsheetId, SheetsSchema.TRANSACTIONS_SHEET, SheetsSchema.TRANSACTIONS_HEADERS)
        if (migrated.isNotEmpty()) {
            service.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "${SheetsSchema.TRANSACTIONS_SHEET}!A2:$lastCol${migrated.size + 1}",
                    ValueRange().setValues(migrated)
                )
                .setValueInputOption("RAW")
                .execute()
        }
        Log.i("SheetsService", "Migrated legacy spreadsheet to schema v2 (${migrated.size} rows)")
    }

    private suspend fun ensureHeaders(
        service: Sheets,
        spreadsheetId: String,
        sheetName: String,
        headers: List<String>,
        isNewSheet: Boolean
    ) {
        if (isNewSheet) {
            writeHeaderRow(service, spreadsheetId, sheetName, headers)
            return
        }
        val lastCol = columnLetter(headers.size)
        val current = readRange(service, spreadsheetId, "$sheetName!A1:$lastCol${1}")?.firstOrNull()
        val safeToRepair = current == null || current.isEmpty() || current.size < headers.size
        if (safeToRepair) {
            writeHeaderRow(service, spreadsheetId, sheetName, headers)
        }
    }

    private suspend fun writeHeaderRow(
        service: Sheets,
        spreadsheetId: String,
        sheetName: String,
        headers: List<String>
    ) {
        val lastCol = columnLetter(headers.size)
        service.spreadsheets().values()
            .update(spreadsheetId, "$sheetName!A1:$lastCol${1}", ValueRange().setValues(listOf(headers)))
            .setValueInputOption("RAW")
            .execute()
    }

    private suspend fun appendRows(
        service: Sheets,
        spreadsheetId: String,
        sheetName: String,
        rows: List<List<Any>>
    ): List<Int> {
        val colCount = when (sheetName) {
            SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS.size
            SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS.size
            else -> 0
        }
        val lastCol = columnLetter(colCount)
        val response = service.spreadsheets().values()
            .append(spreadsheetId, "$sheetName!A:$lastCol", ValueRange().setValues(rows))
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
        return parseStartRows(response?.updates?.updatedRange, rows.size) ?: emptyList()
    }

    private suspend fun updateRow(
        service: Sheets,
        spreadsheetId: String,
        sheetName: String,
        rowIndex: Int,
        values: List<Any>
    ) {
        val colCount = when (sheetName) {
            SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS.size
            SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS.size
            else -> values.size
        }
        val lastCol = columnLetter(colCount)
        service.spreadsheets().values()
            .update(spreadsheetId, "$sheetName!A$rowIndex:$lastCol$rowIndex", ValueRange().setValues(listOf(values)))
            .setValueInputOption("RAW")
            .execute()
    }

    private suspend fun rewriteSheet(
        service: Sheets,
        spreadsheetId: String,
        sheetName: String,
        headers: List<String>,
        dataRows: List<List<Any>>
    ) {
        val lastCol = columnLetter(headers.size)
        service.spreadsheets().values()
            .clear(spreadsheetId, "$sheetName!A1:$lastCol${dataRows.size + 1}", ClearValuesRequest())
            .execute()
        val allRows = listOf(headers) + dataRows
        if (allRows.isNotEmpty()) {
            service.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "$sheetName!A1:$lastCol${allRows.size}",
                    ValueRange().setValues(allRows)
                )
                .setValueInputOption("RAW")
                .execute()
        }
    }

    private suspend fun readRange(
        service: Sheets,
        spreadsheetId: String,
        range: String
    ): List<List<Any>>? {
        return try {
            @Suppress("UNCHECKED_CAST")
            service.spreadsheets().values().get(spreadsheetId, range)
                .execute().values as? List<List<Any>>
        } catch (e: Exception) {
            Log.e("SheetsService", "readRange failed for $range: ${e.message}")
            null
        }
    }

    private fun parseStartRows(updatedRange: String?, count: Int): List<Int>? {
        if (updatedRange == null) return null
        val match = Regex("([A-Z]+)(\\d+)").find(updatedRange.substringAfterLast('!'))
        val start = match?.groupValues?.get(2)?.toIntOrNull() ?: return null
        return (start until start + count).toList()
    }

    private fun columnLetter(columnCount: Int): String {
        var num = columnCount
        val sb = StringBuilder()
        while (num > 0) {
            num--
            sb.insert(0, ('A' + (num % 26)))
            num /= 26
        }
        return sb.toString()
    }
}
