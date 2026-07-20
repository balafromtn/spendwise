package com.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun defaultCategories(): List<CategoryEntity> = listOf(
            CategoryEntity(name = "Food", type = "Expense", isDefault = true),
            CategoryEntity(name = "Transport", type = "Expense", isDefault = true),
            CategoryEntity(name = "Shopping", type = "Expense", isDefault = true),
            CategoryEntity(name = "Bills", type = "Expense", isDefault = true),
            CategoryEntity(name = "Entertainment", type = "Expense", isDefault = true),
            CategoryEntity(name = "Healthcare", type = "Expense", isDefault = true),
            CategoryEntity(name = "Education", type = "Expense", isDefault = true),
            CategoryEntity(name = "Salary", type = "Income", isDefault = true),
            CategoryEntity(name = "Freelance", type = "Income", isDefault = true),
            CategoryEntity(name = "Investments", type = "Income", isDefault = true)
        )
    }
}
