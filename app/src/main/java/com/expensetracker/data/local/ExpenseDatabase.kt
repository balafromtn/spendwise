package com.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity
import java.util.UUID

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        CategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN transactionId TEXT NOT NULL DEFAULT ''")

                val cursor = db.query("SELECT id FROM transactions")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uuid = UUID.randomUUID().toString()
                    db.execSQL(
                        "UPDATE transactions SET transactionId = ? WHERE id = ?",
                        arrayOf(uuid, id)
                    )
                }
                cursor.close()

                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_transactionId ON transactions (transactionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_syncStatus ON transactions (syncStatus)")
            }
        }

        fun defaultCategories(): List<CategoryEntity> = listOf(
            CategoryEntity(name = "Food", type = "Expense", isDefault = true),
            CategoryEntity(name = "Transport", type = "Expense", isDefault = true),
            CategoryEntity(name = "Shopping", type = "Expense", isDefault = true),
            CategoryEntity(name = "Bills", type = "Expense", isDefault = true),
            CategoryEntity(name = "Entertainment", type = "Expense", isDefault = true),
            CategoryEntity(name = "Healthcare", type = "Expense", isDefault = true),
            CategoryEntity(name = "Education", type = "Expense", isDefault = true),
            CategoryEntity(name = "Salary", type = "Income", isDefault = true),
            CategoryEntity(name = "Freelancing", type = "Income", isDefault = true),
            CategoryEntity(name = "Allowance", type = "Income", isDefault = true),
            CategoryEntity(name = "Scholarship", type = "Income", isDefault = true),
            CategoryEntity(name = "Investments", type = "Income", isDefault = true)
        )
    }
}
