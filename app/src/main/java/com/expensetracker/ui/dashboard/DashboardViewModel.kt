package com.expensetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.MonthlySummary
import com.expensetracker.domain.usecase.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: MonthlySummary = MonthlySummary(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val dateUtils = DateUtils()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val month = dateUtils.currentMonthString()
            val weeksElapsed = dateUtils.weeksElapsedInMonth(dateUtils.today())

            combine(
                container.aggregationUseCase.getMonthlySummary(month),
                container.database.transactionDao().getRecentTransactions(10)
            ) { summary, recent ->
                summary.copy(
                    averageWeeklySpend = if (weeksElapsed > 0) summary.totalExpense / weeksElapsed else 0.0
                ) to recent
            }.collect { (summary, recent) ->
                _uiState.value = DashboardUiState(
                    summary = summary,
                    recentTransactions = recent,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
