package com.expensetracker.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object SpreadsheetConfig {
    val spreadsheetIdKey = stringPreferencesKey("spreadsheet_id")

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun getSpreadsheetId(): String {
        val ctx = appContext ?: return ""
        return ctx.dataStore.data.first()[spreadsheetIdKey] ?: ""
    }

    fun setSpreadsheetId(id: String) {
        val ctx = appContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            ctx.dataStore.edit { it[spreadsheetIdKey] = id }
        }
    }
}
