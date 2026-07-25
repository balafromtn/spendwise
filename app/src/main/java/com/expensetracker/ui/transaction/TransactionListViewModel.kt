package com.expensetracker.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionListUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filterType: String? = null,
    val filterCategory: String? = null,
    val filterPaymentMethod: String? = null,
    val filterStartDate: String? = null,
    val filterEndDate: String? = null,
    val isLoading: Boolean = true
)

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private var searchDebounceJob: Job? = null

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

    fun activateSearch() {
        _uiState.value = _uiState.value.copy(isSearchActive = true)
    }

    fun deactivateSearch() {
        _uiState.value = _uiState.value.copy(
            isSearchActive = false,
            searchQuery = ""
        )
        applyFiltersToState()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(300)
            applyFiltersToState()
        }
    }

    fun setFilterType(type: String?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        applyFiltersToState()
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
            searchQuery = "",
            filterType = null,
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
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        return transactions.filter { t ->
            val matchesSearch = query.isEmpty() ||
                    t.notes.lowercase().contains(query) ||
                    t.category.lowercase().contains(query)
            val matchesType = state.filterType == null || t.type == state.filterType
            val matchesCategory = state.filterCategory == null || t.category == state.filterCategory
            val matchesMethod = state.filterPaymentMethod == null || t.paymentMethod == state.filterPaymentMethod
            val matchesStart = state.filterStartDate == null || t.date >= state.filterStartDate
            val matchesEnd = state.filterEndDate == null || t.date <= state.filterEndDate
            matchesSearch && matchesType && matchesCategory && matchesMethod && matchesStart && matchesEnd
        }
    }
}
