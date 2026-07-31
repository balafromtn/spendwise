package com.expensetracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.expensetracker.data.local.ExpenseDatabase
import com.expensetracker.data.remote.AuthManager
import com.expensetracker.data.remote.SheetsService
import com.expensetracker.data.remote.TokenProvider
import com.expensetracker.domain.usecase.AggregationUseCase
import com.expensetracker.domain.usecase.DateUtils
import com.expensetracker.notifications.ReminderScheduler
import com.expensetracker.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(private val context: Context) {

    val database: ExpenseDatabase by lazy {
        val db = Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_tracker.db"
        ).addMigrations(ExpenseDatabase.MIGRATION_1_2).build()

        CoroutineScope(Dispatchers.IO).launch {
            val existing = db.categoryDao().getAllOnce()
            if (existing.isEmpty()) {
                db.categoryDao().insertAll(ExpenseDatabase.defaultCategories())
            } else {
                val existingKeys = existing.map { it.name to it.type }.toSet()
                val missing = ExpenseDatabase.defaultCategories()
                    .filter { (it.name to it.type) !in existingKeys }
                if (missing.isNotEmpty()) {
                    db.categoryDao().insertAll(missing)
                }
            }
        }

        db
    }

    val authManager: AuthManager by lazy {
        AuthManager(context)
    }

    val tokenProvider: TokenProvider by lazy {
        TokenProvider(context, authManager)
    }

    val sheetsService: SheetsService by lazy {
        SheetsService(tokenProvider)
    }

    val aggregationUseCase: AggregationUseCase by lazy {
        AggregationUseCase(database.transactionDao(), database.budgetDao())
    }

    val syncOrchestrator: SyncOrchestrator by lazy {
        SyncOrchestrator(database, sheetsService, aggregationUseCase)
    }

    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(context)
    }

    val dateUtils: DateUtils by lazy {
        DateUtils()
    }
}
