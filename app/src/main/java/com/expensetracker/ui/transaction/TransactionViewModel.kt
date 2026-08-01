package com.expensetracker.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.domain.model.PaymentMethod
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AddTransactionUiState(
    val type: String = "Expense",
    val amount: String = "",
    val category: String = "",
    val date: String = "",
    val time: String = "",
    val notes: String = "",
    val paymentMethod: String = "Cash",
    val isEditing: Boolean = false,
    val editingId: Long = 0,
    val showSuccess: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val dateUtils = DateUtils()

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(
            date = dateUtils.toSheetDate(dateUtils.today()),
            time = dateUtils.toSheetTime(dateUtils.now())
        )
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = _uiState
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type ->
            container.database.categoryDao().getCategoriesByType(type)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateType(type: String) {
        _uiState.value = _uiState.value.copy(type = type, category = "")
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateDate(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun updateTime(time: String) {
        _uiState.value = _uiState.value.copy(time = time)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updatePaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun loadForEdit(transactionId: Long) {
        viewModelScope.launch {
            val transaction = container.transactionRepository.getById(transactionId)
            if (transaction != null) {
                _uiState.value = AddTransactionUiState(
                    type = transaction.type.label,
                    amount = transaction.amount.toString(),
                    category = transaction.category,
                    date = transaction.date,
                    time = transaction.time,
                    notes = transaction.notes,
                    paymentMethod = transaction.paymentMethod.label,
                    isEditing = true,
                    editingId = transaction.id
                )
            }
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.isSaving) return

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = "Please enter a valid amount")
            return
        }
        if (state.category.isBlank()) {
            _uiState.value = state.copy(error = "Please select a category")
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val dateObj = dateUtils.parseSheetDate(state.date)
            val month = dateUtils.toMonthString(dateObj)
            val weekNo = dateUtils.toWeekNumber(dateObj)

            val existing = if (state.isEditing) {
                container.transactionRepository.getById(state.editingId)
            } else {
                null
            }

            val entity = Transaction(
                id = if (state.isEditing) state.editingId else 0,
                transactionId = existing?.transactionId ?: UUID.randomUUID().toString(),
                date = state.date,
                time = state.time,
                type = try { TransactionType.valueOf(state.type.uppercase()) } catch (e: Exception) { TransactionType.EXPENSE },
                category = state.category,
                amount = amount,
                paymentMethod = try { PaymentMethod.valueOf(state.paymentMethod.uppercase()) } catch (e: Exception) { PaymentMethod.CASH },
                notes = state.notes,
                month = month,
                weekNo = weekNo,
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis(),
                version = if (existing != null) existing.version + 1 else 1
            )

            if (state.isEditing) {
                container.transactionRepository.update(entity)
            } else {
                container.transactionRepository.insert(entity)
            }

            SyncWorker.enqueueImmediateSync(getApplication())
            _uiState.value = _uiState.value.copy(
                showSuccess = true,
                error = null,
                isSaving = false
            )
        }
    }

    fun resetForm() {
        _uiState.value = AddTransactionUiState(
            date = dateUtils.toSheetDate(dateUtils.today()),
            time = dateUtils.toSheetTime(dateUtils.now())
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(showSuccess = false)
    }
}
