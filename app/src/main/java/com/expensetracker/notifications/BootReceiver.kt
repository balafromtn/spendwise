package com.expensetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expensetracker.di.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.dataStore.data.first()
                val hour = prefs[ReminderScheduler.HOUR_KEY] ?: 20
                val minute = prefs[ReminderScheduler.MINUTE_KEY] ?: 0
                ReminderScheduler(context).schedule(hour, minute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
