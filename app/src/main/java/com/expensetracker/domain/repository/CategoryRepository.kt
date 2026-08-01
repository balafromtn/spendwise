package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesByType(type: String): Flow<List<Category>>
    fun getAllCategories(): Flow<List<Category>>
    suspend fun insert(category: Category)
    suspend fun insertAll(categories: List<Category>)
    suspend fun update(category: Category)
    suspend fun delete(category: Category)
    suspend fun getCount(): Int
}
