package com.expensetracker.domain.model

data class Transaction(
    val id: Long = 0,
    val date: String = "",
    val time: String = "",
    val type: String = "Expense",
    val category: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val month: String = "",
    val weekNo: Int = 0,
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val sheetRowId: Int? = null
)

enum class TransactionType { INCOME, EXPENSE }

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    BANK("Bank")
}

data class MonthlySummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val transactionCount: Int = 0,
    val highestIncome: Double = 0.0,
    val lowestIncome: Double = 0.0,
    val highestExpense: Double = 0.0,
    val lowestExpense: Double = 0.0,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList()
)

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val type: String
)

data class Budget(
    val id: Long = 0,
    val month: String = "",
    val category: String = "",
    val budgetAmount: Double = 0.0,
    val spentSoFar: Double = 0.0,
    val syncStatus: String = "PENDING",
    val sheetRowId: Int? = null
) {
    val utilizationPercent: Double
        get() = if (budgetAmount > 0) (spentSoFar / budgetAmount) * 100 else 0.0

    val isOverBudget: Boolean
        get() = spentSoFar > budgetAmount

    val isNearLimit: Boolean
        get() = utilizationPercent in 75.0..100.0
}

data class Category(
    val id: Long = 0,
    val name: String = "",
    val type: String = "Expense",
    val isCustom: Boolean = false,
    val isDefault: Boolean = false
)
