package com.expensetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.expensetracker.di.AppContainer
import com.expensetracker.di.SpreadsheetConfig
import com.expensetracker.data.remote.AuthManager

class ExpenseTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Initialize configs
        SpreadsheetConfig.init(this)
        val spreadsheetId = BuildConfig.SPREADSHEET_ID
        if (spreadsheetId.isNotBlank() && spreadsheetId != "YOUR_SPREADSHEET_ID_HERE") {
            SpreadsheetConfig.setSpreadsheetId(spreadsheetId)
        }

        val webClientId = BuildConfig.WEB_CLIENT_ID
        if (webClientId.isNotBlank() && webClientId != "YOUR_WEB_CLIENT_ID_HERE") {
            AuthManager.init(webClientId)
        }
        AuthManager.setContext(this)
        AuthManager.updateSignInState()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily expense logging reminders"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "daily_reminder"
    }
}
