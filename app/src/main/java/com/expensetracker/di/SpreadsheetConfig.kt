package com.expensetracker.di

import android.content.Context
import android.content.SharedPreferences

object SpreadsheetConfig {
    private const val PREFS_NAME = "spreadsheet_config"
    private const val KEY_SPREADSHEET_ID = "spreadsheet_id"

    private var spreadsheetId: String = ""

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        spreadsheetId = prefs.getString(KEY_SPREADSHEET_ID, "") ?: ""
    }

    fun setSpreadsheetId(id: String) {
        spreadsheetId = id
    }

    fun getSpreadsheetId(): String = spreadsheetId
}
