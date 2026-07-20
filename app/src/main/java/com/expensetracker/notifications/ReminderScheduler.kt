package com.expensetracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.expensetracker.di.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    companion object {
        val HOUR_KEY = intPreferencesKey("reminder_hour")
        val MINUTE_KEY = intPreferencesKey("reminder_minute")
    }

    fun schedule(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun saveReminderTime(hour: Int, minute: Int) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[HOUR_KEY] = hour
                prefs[MINUTE_KEY] = minute
            }
        }
        schedule(hour, minute)
    }

    fun getSavedReminderTime(): Pair<Int, Int> {
        return runBlocking {
            val prefs = context.dataStore.data.first()
            val hour = prefs[HOUR_KEY] ?: 20
            val minute = prefs[MINUTE_KEY] ?: 0
            Pair(hour, minute)
        }
    }
}
