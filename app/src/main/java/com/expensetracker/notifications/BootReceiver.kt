package com.expensetracker.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expensetracker.di.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val dataStore = context.dataStore
            val hour = runBlocking { dataStore.data.first()[com.expensetracker.notifications.ReminderScheduler.HOUR_KEY] } ?: 20
            val minute = runBlocking { dataStore.data.first()[com.expensetracker.notifications.ReminderScheduler.MINUTE_KEY] } ?: 0
            ReminderScheduler(context).schedule(hour, minute)
        }
    }
}
