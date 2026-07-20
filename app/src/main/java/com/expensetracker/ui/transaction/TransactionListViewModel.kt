package com.expensetracker.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionListUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val filterCategory: String? = null,
    val filterPaymentMethod: String? = null,
    val filterStartDate: String? = null,
    val filterEndDate: String? = null,
    val isLoading: Boolean = true
)

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            container.database.transactionDao().getAllTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    filteredTransactions = applyFilters(transactions),
                    isLoading = false
                )
            }
        }
    }

    fun setFilterCategory(category: String?) {
        _uiState.value = _uiState.value.copy(filterCategory = category)
        applyFiltersToState()
    }

    fun setFilterPaymentMethod(method: String?) {
        _uiState.value = _uiState.value.copy(filterPaymentMethod = method)
        applyFiltersToState()
    }

    fun setFilterDateRange(startDate: String?, endDate: String?) {
        _uiState.value = _uiState.value.copy(
            filterStartDate = startDate,
            filterEndDate = endDate
        )
        applyFiltersToState()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filterCategory = null,
            filterPaymentMethod = null,
            filterStartDate = null,
            filterEndDate = null
        )
        applyFiltersToState()
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            container.database.transactionDao().delete(transaction)
        }
    }

    private fun applyFiltersToState() {
        _uiState.value = _uiState.value.copy(
            filteredTransactions = applyFilters(_uiState.value.transactions)
        )
    }

    private fun applyFilters(transactions: List<TransactionEntity>): List<TransactionEntity> {
        return transactions.filter { t ->
            val state = _uiState.value
            val matchesCategory = state.filterCategory == null || t.category == state.filterCategory
            val matchesMethod = state.filterPaymentMethod == null || t.paymentMethod == state.filterPaymentMethod
            val matchesStart = state.filterStartDate == null || t.date >= state.filterStartDate
            val matchesEnd = state.filterEndDate == null || t.date <= state.filterEndDate
            matchesCategory && matchesMethod && matchesStart && matchesEnd
        }
    }
}
