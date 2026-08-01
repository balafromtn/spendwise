package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.mapper.toDomain
import com.expensetracker.data.mapper.toEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getBudgetsByMonth(month: String): Flow<List<Budget>> {
        return dao.getBudgetsByMonth(month).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBudget(month: String, category: String): Budget? {
        return dao.getBudget(month, category)?.toDomain()
    }

    override suspend fun insert(budget: Budget) {
        dao.insert(budget.toEntity())
    }

    override suspend fun update(budget: Budget) {
        val existing = dao.getBudget(budget.month, budget.category)
        if (existing != null) {
            val updated = budget.toEntity(existing.budgetId).copy(
                version = existing.version + 1,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
            dao.update(updated)
        }
    }

    override suspend fun delete(budget: Budget) {
        dao.markDeleted(budget.id, System.currentTimeMillis())
    }
}
