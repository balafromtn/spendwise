package com.expensetracker.ui.settings

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.data.remote.AuthManager
import com.expensetracker.di.dataStore
import com.expensetracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val userEmail: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ExpenseTrackerApp).container
    private val prefs = application.dataStore

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val (hour, minute) = container.reminderScheduler.getSavedReminderTime()
            val data = prefs.data.first()
            val themeModeStr = data[THEME_KEY] ?: ThemeMode.SYSTEM.name
            val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
            _uiState.value = SettingsUiState(
                themeMode = themeMode,
                reminderHour = hour,
                reminderMinute = minute,
                userEmail = container.tokenProvider.getUserEmail() ?: ""
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        viewModelScope.launch {
            prefs.edit { it[THEME_KEY] = mode.name }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
        viewModelScope.launch {
            container.reminderScheduler.saveReminderTime(hour, minute)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val authManager = AuthManager(getApplication())
            authManager.signOut()
        }
    }

    companion object {
        val THEME_KEY = androidx.datastore.preferences.core.stringPreferencesKey("theme_mode")
    }
}
