package com.expensetracker.data.remote

object SheetsSchema {
    const val TRANSACTIONS_SHEET = "Transactions"
    const val BUDGETS_SHEET = "Budgets"
    const val SUMMARY_SHEET = "Summary"

    val TRANSACTIONS_HEADERS = listOf(
        "Date", "Time", "Type", "Category", "Amount (\u20B9)",
        "Payment Method", "Notes", "Month", "Week No."
    )

    val BUDGETS_HEADERS = listOf(
        "Month (e.g. \"Apr 2026\")", "Category",
        "Budget Amount (\u20B9)", "Spent So Far (\u20B9)"
    )

    val SUMMARY_HEADERS = listOf("Metric", "Value")

    fun transactionRow(transaction: com.expensetracker.data.local.entity.TransactionEntity): List<Any> {
        return listOf(
            transaction.date,
            transaction.time,
            transaction.type,
            transaction.category,
            transaction.amount,
            transaction.paymentMethod,
            transaction.notes,
            transaction.month,
            transaction.weekNo
        )
    }

    fun budgetRow(budget: com.expensetracker.data.local.entity.BudgetEntity): List<Any> {
        return listOf(
            budget.month,
            budget.category,
            budget.budgetAmount,
            budget.spentSoFar
        )
    }

    fun summaryRows(
        totalIncome: Double,
        totalExpense: Double,
        netSavings: Double,
        totalTransactions: Int,
        highestIncome: Double,
        lowestIncome: Double,
        highestExpense: Double,
        lowestExpense: Double,
        incomeBreakdown: List<Pair<String, Double>>,
        expenseBreakdown: List<Pair<String, Double>>
    ): List<List<Any>> {
        val rows = mutableListOf<List<Any>>()
        rows.add(listOf("Total Income", totalIncome))
        rows.add(listOf("Total Expense", totalExpense))
        rows.add(listOf("Net Balance/Savings", netSavings))
        rows.add(listOf("Total Transactions", totalTransactions))
        rows.add(listOf("Highest Income", highestIncome))
        rows.add(listOf("Lowest Income", lowestIncome))
        rows.add(listOf("Highest Expense", highestExpense))
        rows.add(listOf("Lowest Expense", lowestExpense))
        rows.add(listOf("---", "---"))
        rows.add(listOf("Category", "Amount | %"))
        for ((cat, amt) in incomeBreakdown) {
            val pct = if (totalIncome > 0) (amt / totalIncome) * 100 else 0.0
            rows.add(listOf("$cat (Income)", "${"%.2f".format(amt)} | ${"%.1f".format(pct)}%"))
        }
        for ((cat, amt) in expenseBreakdown) {
            val pct = if (totalExpense > 0) (amt / totalExpense) * 100 else 0.0
            rows.add(listOf("$cat (Expense)", "${"%.2f".format(amt)} | ${"%.1f".format(pct)}%"))
        }
        return rows
    }
}
