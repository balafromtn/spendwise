package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsByMonth(month: String): Flow<List<Budget>>
    suspend fun getBudget(month: String, category: String): Budget?
    suspend fun insert(budget: Budget)
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
}
