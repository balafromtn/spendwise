package com.expensetracker.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.domain.model.MonthlySummary
import com.expensetracker.domain.usecase.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val dateUtils = DateUtils()
    private var loadJob: Job? = null

    private val _selectedMonth = MutableStateFlow(dateUtils.currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _summary = MutableStateFlow(MonthlySummary())
    val summary: StateFlow<MonthlySummary> = _summary.asStateFlow()

    init {
        loadReport()
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        loadReport()
    }

    fun loadReport() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            container.aggregationUseCase.getMonthlySummary(_selectedMonth.value).collect {
                _summary.value = it
            }
        }
    }
}
