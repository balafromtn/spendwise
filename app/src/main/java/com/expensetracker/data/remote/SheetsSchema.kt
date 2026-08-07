package com.expensetracker.data.remote

object SheetsSchema {
    const val TRANSACTIONS_SHEET = "Transactions"
    const val BUDGETS_SHEET = "Budgets"
    const val CATEGORIES_SHEET = "Categories"
    const val METADATA_SHEET = "Metadata"

    const val SCHEMA_VERSION = 2

    const val KEY_SCHEMA_VERSION = "SchemaVersion"
    const val KEY_LAST_SYNC = "LastSync"
    const val KEY_APP_VERSION = "AppVersion"
    const val KEY_USER_EMAIL = "UserEmail"
    const val KEY_DEVICE_ID = "DeviceId"
    const val KEY_LAST_VALIDATION = "LastValidation"

    val TRANSACTIONS_HEADERS = listOf(
        "UUID", "Date", "Type", "Category", "Amount (\u20B9)",
        "Payment Method", "Notes", "Created Time", "Updated Time", "Version", "Deleted"
    )

    val BUDGETS_HEADERS = listOf(
        "UUID", "Month", "Category", "Budget Amount (\u20B9)", "Spent So Far (\u20B9)",
        "Updated Time", "Version", "Deleted"
    )

    val CATEGORIES_HEADERS = listOf("Category", "Type", "Source", "Deleted")

    val METADATA_HEADERS = listOf("Key", "Value")

    // Legacy v1 Transactions headers (current installs), used for migration detection.
    val LEGACY_TRANSACTIONS_HEADERS = listOf(
        "Date", "Time", "Type", "Category", "Amount (\u20B9)",
        "Payment Method", "Notes", "Month", "Week No.", "Transaction ID"
    )

    fun transactionRow(transaction: com.expensetracker.data.local.entity.TransactionEntity): List<Any> {
        return listOf(
            transaction.transactionId,
            transaction.date,
            transaction.type,
            transaction.category,
            transaction.amount,
            transaction.paymentMethod,
            transaction.notes,
            transaction.createdAt,
            transaction.updatedAt,
            transaction.version,
            if (transaction.deleted) "TRUE" else "FALSE"
        )
    }

    fun budgetRow(budget: com.expensetracker.data.local.entity.BudgetEntity): List<Any> {
        return listOf(
            budget.budgetId,
            budget.month,
            budget.category,
            budget.budgetAmount,
            budget.spentSoFar,
            budget.updatedAt,
            budget.version,
            if (budget.deleted) "TRUE" else "FALSE"
        )
    }

    fun categoryRow(category: com.expensetracker.data.local.entity.CategoryEntity): List<Any> {
        return listOf(
            category.name,
            category.type,
            if (category.isDefault) "Default" else "Custom",
            "FALSE"
        )
    }

    fun metadataRows(values: Map<String, String>): List<List<Any>> {
        return listOf(METADATA_HEADERS) +
            values.map { (key, value) -> listOf(key, value) }
    }

    fun parseMetadata(rows: List<List<Any>>): Map<String, String> {
        return rows.drop(1).mapNotNull { row ->
            if (row.size >= 2) row[0].toString() to row[1].toString() else null
        }.toMap()
    }
}
