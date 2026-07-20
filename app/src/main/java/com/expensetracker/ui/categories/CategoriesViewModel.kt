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

    val categories: StateFlow<List<CategoryEntity>> = container.database.categoryDao()
        .getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectType(type: String) {
        _selectedType.value = type
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            container.database.categoryDao().insert(
                CategoryEntity(name = name, type = type, isCustom = true)
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (!category.isDefault) {
            viewModelScope.launch {
                container.database.categoryDao().delete(category)
            }
        }
    }
}
