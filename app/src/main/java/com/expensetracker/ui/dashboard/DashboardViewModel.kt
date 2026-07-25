package com.expensetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.MonthlySummary
import com.expensetracker.domain.usecase.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val recentTransactions = container.database.transactionDao().getRecentTransactions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val month = dateUtils.currentMonthString()
            val weekNo = dateUtils.currentWeekNumber()

            val avgSpendFlow = container.database.transactionDao()
                .getAverageWeeklySpend(weekNo, month)

            combine(
                container.aggregationUseCase.getMonthlySummary(month),
                avgSpendFlow
            ) { summary, avgSpend ->
                summary.copy(averageWeeklySpend = avgSpend ?: 0.0)
            }.collect { summary ->
                _uiState.value = DashboardUiState(
                    summary = summary,
                    recentTransactions = recentTransactions.value,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
