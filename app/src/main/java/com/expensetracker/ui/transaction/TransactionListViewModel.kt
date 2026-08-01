package com.expensetracker.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.sync.SyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.model.PaymentMethod

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
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
    private val dateUtils = DateUtils()
    private var searchDebounceJob: Job? = null

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            container.transactionRepository.getAllTransactions().collect { transactions ->
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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            container.transactionRepository.delete(transaction)
            SyncWorker.enqueueImmediateSync(getApplication())
        }
    }

    private fun applyFiltersToState() {
        _uiState.value = _uiState.value.copy(
            filteredTransactions = applyFilters(_uiState.value.transactions)
        )
    }

    private fun applyFilters(transactions: List<Transaction>): List<Transaction> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        return transactions.filter { t ->
            val matchesSearch = query.isEmpty() ||
                    t.notes.lowercase().contains(query) ||
                    t.category.lowercase().contains(query)
            val matchesType = state.filterType == null || t.type.label == state.filterType
            val matchesCategory = state.filterCategory == null || t.category == state.filterCategory
            val matchesMethod = state.filterPaymentMethod == null || t.paymentMethod.label == state.filterPaymentMethod
            val matchesStart = state.filterStartDate == null ||
                    dateUtils.parseSheetDate(t.date) >= dateUtils.parseSheetDate(state.filterStartDate)
            val matchesEnd = state.filterEndDate == null ||
                    dateUtils.parseSheetDate(t.date) <= dateUtils.parseSheetDate(state.filterEndDate)
            matchesSearch && matchesType && matchesCategory && matchesMethod && matchesStart && matchesEnd
        }
    }
}
