package com.expensetracker.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val dateUtils = DateUtils()

    private val _selectedMonth = MutableStateFlow(dateUtils.currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    init {
        loadBudgets()
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            container.aggregationUseCase.getBudgetsWithSpending(_selectedMonth.value).collect {
                _budgets.value = it
            }
        }
    }

    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val existing = container.database.budgetDao().getBudget(_selectedMonth.value, category)
            if (existing != null) {
                container.database.budgetDao().update(
                    existing.copy(budgetAmount = amount, syncStatus = "PENDING")
                )
            } else {
                container.database.budgetDao().insert(
                    BudgetEntity(
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
            container.database.budgetDao().delete(
                BudgetEntity(
                    id = budget.id,
                    month = budget.month,
                    category = budget.category,
                    budgetAmount = budget.budgetAmount,
                    spentSoFar = budget.spentSoFar
                )
            )
            loadBudgets()
        }
    }
}
