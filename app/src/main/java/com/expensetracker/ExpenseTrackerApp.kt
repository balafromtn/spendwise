package com.expensetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.expensetracker.di.AppContainer
import com.expensetracker.di.SpreadsheetConfig
import com.expensetracker.data.remote.AuthManager
import com.expensetracker.sync.SyncWorker

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

        container.authManager.updateSignInState()

        SyncWorker.schedulePeriodicSync(this)

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
