package com.expensetracker.data.remote

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
                val headers = when (sheetName) {
                    SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS
                    SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS
                    SheetsSchema.SUMMARY_SHEET -> SheetsSchema.SUMMARY_HEADERS
                    else -> emptyList()
                }
                Request().setAddSheet(
                    AddSheetRequest().setProperties(
                        com.google.api.services.sheets.v4.model.SheetProperties()
                            .setTitle(sheetName)
                    )
                ).also { _ ->
                    // Headers will be written separately
                }
            }

            val batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(addSheetRequests)
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdate).execute()

            // Write headers to each newly created sheet
            for (sheetName in sheetsToCreate) {
                val headers = when (sheetName) {
                    SheetsSchema.TRANSACTIONS_SHEET -> SheetsSchema.TRANSACTIONS_HEADERS
                    SheetsSchema.BUDGETS_SHEET -> SheetsSchema.BUDGETS_HEADERS
                    SheetsSchema.SUMMARY_SHEET -> SheetsSchema.SUMMARY_HEADERS
                    else -> continue
                }
                val headerRange = "$sheetName!A1"
                val headerValues = ValueRange().setValues(listOf(headers))
                service.spreadsheets().values()
                    .update(spreadsheetId, headerRange, headerValues)
                    .setValueInputOption("RAW")
                    .execute()
            }
        }
    }

    suspend fun appendTransactions(
        spreadsheetId: String,
        transactions: List<TransactionEntity>
    ) = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext
        if (transactions.isEmpty()) return@withContext
        val rows = transactions.map { SheetsSchema.transactionRow(it) }
        val range = "${SheetsSchema.TRANSACTIONS_SHEET}!A:I"
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
        val range = "${SheetsSchema.BUDGETS_SHEET}!A:D"
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
            val range = "${SheetsSchema.TRANSACTIONS_SHEET}!A:I"
            val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
            response.getValues() ?: emptyList()
        }

    suspend fun readAllBudgets(spreadsheetId: String): List<List<Any>> =
        withContext(Dispatchers.IO) {
            val service = getService() ?: return@withContext emptyList()
            val range = "${SheetsSchema.BUDGETS_SHEET}!A:D"
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
