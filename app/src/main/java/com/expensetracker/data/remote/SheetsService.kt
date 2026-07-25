package com.expensetracker.data.remote

import android.util.Log
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SheetsService(private val tokenProvider: TokenProvider) {

    private fun getService(): Sheets? = tokenProvider.getSheetsService()

    suspend fun ensureSheetsExist(spreadsheetId: String) = withContext(Dispatchers.IO) {
        try {
            val service = getService() ?: throw IllegalStateException("Not authenticated")
            val spreadsheet = service.spreadsheets().get(spreadsheetId)
                .setFields("sheets.properties")
                .execute()

            val existingSheets = spreadsheet.sheets?.map { it.properties?.title } ?: emptyList()
            val sheetsToCreate = mutableListOf<String>()

            if (SheetsSchema.TRANSACTIONS_SHEET !in existingSheets) {
                sheetsToCreate.add(SheetsSchema.TRANSACTIONS_SHEET)
            }
            if (SheetsSchema.BUDGETS_SHEET !in existingSheets) {
                sheetsToCreate.add(SheetsSchema.BUDGETS_SHEET)
            }
            if (SheetsSchema.SUMMARY_SHEET !in existingSheets) {
                sheetsToCreate.add(SheetsSchema.SUMMARY_SHEET)
            }

            if (sheetsToCreate.isNotEmpty()) {
                val addSheetRequests = sheetsToCreate.map { sheetName ->
                    Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            com.google.api.services.sheets.v4.model.SheetProperties()
                                .setTitle(sheetName)
                        )
                    )
                }

                val batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(addSheetRequests)
                service.spreadsheets().batchUpdate(spreadsheetId, batchUpdate).execute()

                for (sheetName in sheetsToCreate) {
                    val headers = when (sheetName) {
                        SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS
                        SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS
                        SheetsSchema.SUMMARY_SHEET -> SheetsSchema.SUMMARY_HEADERS
                        else -> continue
                    }
                    val lastCol = ('A' + headers.size - 1).toChar()
                    val headerRange = "$sheetName!A1:${lastCol}1"
                    val headerValues = ValueRange().setValues(listOf(headers))
                    service.spreadsheets().values()
                        .update(spreadsheetId, headerRange, headerValues)
                        .setValueInputOption("RAW")
                        .execute()
                }
            }
        } catch (e: Exception) {
            Log.e("SheetsService", "ensureSheetsExist failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun appendTransactions(
        spreadsheetId: String,
        transactions: List<TransactionEntity>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        if (transactions.isEmpty()) return@withContext
        val rows = transactions.map { SheetsSchema.transactionRow(it) }
        val lastCol = ('A' + SheetsSchema.TRANSACTIONS_HEADERS.size - 1).toChar()
        val range = "${SheetsSchema.TRANSACTIONS_SHEET}!A:${lastCol}"
        val body = ValueRange().setValues(rows)
        service.spreadsheets().values()
            .append(spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }

    suspend fun appendBudgets(
        spreadsheetId: String,
        budgets: List<BudgetEntity>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        if (budgets.isEmpty()) return@withContext
        val rows = budgets.map { SheetsSchema.budgetRow(it) }
        val lastCol = ('A' + SheetsSchema.BUDGETS_HEADERS.size - 1).toChar()
        val range = "${SheetsSchema.BUDGETS_SHEET}!A:${lastCol}"
        val body = ValueRange().setValues(rows)
        service.spreadsheets().values()
            .append(spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }

    suspend fun rewriteSummary(
        spreadsheetId: String,
        rows: List<List<Any>>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        val range = "${SheetsSchema.SUMMARY_SHEET}!A:B"
        val body = ValueRange().setValues(rows)
        service.spreadsheets().values()
            .update(spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
    }

    suspend fun readAllTransactions(spreadsheetId: String): List<List<Any>> =
        withContext(Dispatchers.IO) {
            val service = getService() ?: return@withContext emptyList()
            val lastCol = ('A' + SheetsSchema.TRANSACTIONS_HEADERS.size - 1).toChar()
            val range = "${SheetsSchema.TRANSACTIONS_SHEET}!A:${lastCol}"
            val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
            response.getValues() ?: emptyList()
        }

    suspend fun readAllBudgets(spreadsheetId: String): List<List<Any>> =
        withContext(Dispatchers.IO) {
            val service = getService() ?: return@withContext emptyList()
            val lastCol = ('A' + SheetsSchema.BUDGETS_HEADERS.size - 1).toChar()
            val range = "${SheetsSchema.BUDGETS_SHEET}!A:${lastCol}"
            val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
            response.getValues() ?: emptyList()
        }

    suspend fun readAllSummary(spreadsheetId: String): List<List<Any>> =
        withContext(Dispatchers.IO) {
            val service = getService() ?: return@withContext emptyList()
            val range = "${SheetsSchema.SUMMARY_SHEET}!A:B"
            val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
            response.getValues() ?: emptyList()
        }
}
