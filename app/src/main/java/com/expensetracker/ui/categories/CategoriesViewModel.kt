package com.expensetracker.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container

    private val _selectedType = MutableStateFlow("Expense")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = container.database.categoryDao()
        .getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectType(type: String) {
        _selectedType.value = type
    }

    suspend fun addCategory(name: String, type: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _error.value = "Category name cannot be empty"
            return false
        }
        val existing = container.database.categoryDao().getAllOnce()
        if (existing.any { it.type == type && it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = "Category \"$trimmed\" already exists"
            return false
        }
        container.database.categoryDao().insert(
            CategoryEntity(name = trimmed, type = type, isCustom = true)
        )
        _error.value = null
        return true
    }

    suspend fun updateCategory(category: CategoryEntity, newName: String): Boolean {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            _error.value = "Category name cannot be empty"
            return false
        }
        val existing = container.database.categoryDao().getAllOnce()
        if (existing.any {
                it.id != category.id && it.type == category.type && it.name.equals(trimmed, ignoreCase = true)
            }
        ) {
            _error.value = "Category \"$trimmed\" already exists"
            return false
        }
        container.database.categoryDao().update(category.copy(name = trimmed))
        if (category.name != trimmed) {
            container.database.transactionDao().renameTransactions(
                category.name, trimmed, category.type, System.currentTimeMillis()
            )
        }
        _error.value = null
        return true
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            container.database.categoryDao().delete(category)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
