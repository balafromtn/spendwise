package com.expensetracker

import com.expensetracker.domain.model.Budget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetLogicTest {

    @Test
    fun `utilization percent calculates correctly`() {
        val budget = Budget(
            budgetAmount = 10000.0,
            spentSoFar = 5000.0
        )
        assertEquals(50.0, budget.utilizationPercent, 0.01)
    }

    @Test
    fun `isOverBudget when spent exceeds budget`() {
        val budget = Budget(
            budgetAmount = 10000.0,
            spentSoFar = 12000.0
        )
        assertTrue(budget.isOverBudget)
    }

    @Test
    fun `isNotOverBudget when spent is within budget`() {
        val budget = Budget(
            budgetAmount = 10000.0,
            spentSoFar = 8000.0
        )
        assertFalse(budget.isOverBudget)
    }

    @Test
    fun `isNearLimit when between 75 and 100 percent`() {
        val budget = Budget(
            budgetAmount = 10000.0,
            spentSoFar = 8000.0
        )
        assertTrue(budget.isNearLimit)
    }

    @Test
    fun `isNotNearLimit when below 75 percent`() {
        val budget = Budget(
            budgetAmount = 10000.0,
            spentSoFar = 5000.0
        )
        assertFalse(budget.isNearLimit)
    }

    @Test
    fun `utilization with zero budget`() {
        val budget = Budget(
            budgetAmount = 0.0,
            spentSoFar = 100.0
        )
        assertEquals(0.0, budget.utilizationPercent, 0.01)
    }
}
