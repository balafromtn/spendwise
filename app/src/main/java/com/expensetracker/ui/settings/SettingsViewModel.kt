package com.expensetracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.remote.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean? = null, // null = system default
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val userEmail: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        val (hour, minute) = container.reminderScheduler.getSavedReminderTime()
        _uiState.value = SettingsUiState(
            reminderHour = hour,
            reminderMinute = minute,
            userEmail = container.tokenProvider.getUserEmail() ?: ""
        )
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
        container.reminderScheduler.saveReminderTime(hour, minute)
    }

    fun signOut() {
        viewModelScope.launch {
            val authManager = AuthManager(getApplication())
            authManager.signOut()
        }
    }
}
