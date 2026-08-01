package com.expensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.expensetracker.di.dataStore
import com.expensetracker.ui.ExpenseTrackerMainScreen
import com.expensetracker.ui.settings.SettingsViewModel
import com.expensetracker.ui.theme.ExpenseTrackerTheme
import com.expensetracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {

    private val themeMode by lazy {
        dataStore.data.map { prefs ->
            val name = prefs[SettingsViewModel.THEME_KEY] ?: ThemeMode.SYSTEM.name
            try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.SYSTEM }
        }.stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
        }

        setContent {
            val currentThemeMode by themeMode.collectAsState()
            ExpenseTrackerTheme(themeMode = currentThemeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = application as com.expensetracker.ExpenseTrackerApp
                    ExpenseTrackerMainScreen(authManager = app.container.authManager)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1001
    }
}
