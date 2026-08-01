package com.expensetracker.di

import android.content.Context
import android.content.SharedPreferences

object SpreadsheetConfig {
    private const val PREFS_NAME = "spreadsheet_config"
    private const val KEY_SPREADSHEET_ID = "spreadsheet_id"

    private var appContext: Context? = null
    private var spreadsheetId: String = ""

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        spreadsheetId = prefs.getString(KEY_SPREADSHEET_ID, "") ?: ""
    }

    fun setSpreadsheetId(id: String) {
        spreadsheetId = id
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_SPREADSHEET_ID, id)
            ?.apply()
    }

    fun getSpreadsheetId(): String = spreadsheetId
}
