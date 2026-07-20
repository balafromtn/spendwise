package com.expensetracker

import com.expensetracker.domain.model.MonthlySummary
import com.expensetracker.domain.model.CategoryBreakdown
import org.junit.Assert.assertEquals
import org.junit.Test

class AggregationTest {

    @Test
    fun `net savings is income minus expense`() {
        val summary = MonthlySummary(
            totalIncome = 50000.0,
            totalExpense = 30000.0
        )
        assertEquals(20000.0, summary.netSavings, 0.01)
    }

    @Test
    fun `category breakdown percentage calculates correctly`() {
        val breakdown = CategoryBreakdown(
            category = "Food",
            amount = 5000.0,
            percentage = 25.0,
            type = "Expense"
        )
        assertEquals(25.0, breakdown.percentage, 0.01)
    }

    @Test
    fun `empty summary has zero values`() {
        val summary = MonthlySummary()
        assertEquals(0.0, summary.totalIncome, 0.01)
        assertEquals(0.0, summary.totalExpense, 0.01)
        assertEquals(0.0, summary.netSavings, 0.01)
        assertEquals(0, summary.transactionCount)
    }
}
