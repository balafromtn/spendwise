package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.mapper.toDomain
import com.expensetracker.data.mapper.toEntity
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {
    override fun getCategoriesByType(type: String): Flow<List<Category>> {
        return dao.getCategoriesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(category: Category) {
        dao.insert(category.toEntity())
    }

    override suspend fun insertAll(categories: List<Category>) {
        dao.insertAll(categories.map { it.toEntity() })
    }

    override suspend fun update(category: Category) {
        dao.update(category.toEntity())
    }

    override suspend fun delete(category: Category) {
        dao.delete(category.toEntity())
    }

    override suspend fun getCount(): Int {
        return dao.getCount()
    }
}
