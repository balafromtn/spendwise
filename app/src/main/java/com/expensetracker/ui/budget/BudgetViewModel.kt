package com.expensetracker.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.sync.SyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val dateUtils = DateUtils()
    private var loadJob: Job? = null

    private val _selectedMonth = MutableStateFlow(dateUtils.currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    val expenseCategories: StateFlow<List<com.expensetracker.domain.model.Category>> = container.categoryRepository
        .getCategoriesByType("Expense")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBudgets()
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        loadBudgets()
    }

    fun loadBudgets() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            container.aggregationUseCase.getBudgetsWithSpending(_selectedMonth.value).collect {
                _budgets.value = it
            }
        }
    }

    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val existing = container.budgetRepository.getBudget(_selectedMonth.value, category)
            if (existing != null) {
                container.budgetRepository.update(
                    existing.copy(
                        budgetAmount = amount
                    )
                )
            } else {
                container.budgetRepository.insert(
                    Budget(
                        month = _selectedMonth.value,
                        category = category,
                        budgetAmount = amount,
                        syncStatus = "PENDING"
                    )
                )
            }
            SyncWorker.enqueueImmediateSync(getApplication())
            loadBudgets()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            container.budgetRepository.delete(budget)
            SyncWorker.enqueueImmediateSync(getApplication())
            loadBudgets()
        }
    }
}
