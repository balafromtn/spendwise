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

    val transactions = container.database.transactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val month = dateUtils.currentMonthString()
            container.aggregationUseCase.getMonthlySummary(month).collect { summary ->
                _uiState.value = DashboardUiState(
                    summary = summary,
                    recentTransactions = transactions.value.take(10),
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
