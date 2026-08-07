package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.PaymentMethod
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import android.util.Log

private inline fun <reified T : Enum<T>> safeEnumParse(value: String, default: T): T {
    return try {
        enumValueOf<T>(value.uppercase())
    } catch (e: Exception) {
        Log.w("Mappers", "Unknown ${T::class.simpleName} value: '$value', defaulting to ${default.name}")
        default
    }
}

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = this.id,
    transactionId = this.transactionId,
    date = this.date,
    time = this.time,
    type = safeEnumParse(this.type, TransactionType.EXPENSE),
    category = this.category,
    amount = this.amount,
    paymentMethod = safeEnumParse(this.paymentMethod, PaymentMethod.CASH),
    notes = this.notes,
    month = this.month,
    weekNo = this.weekNo,
    syncStatus = this.syncStatus,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    version = this.version,
    deleted = this.deleted
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    transactionId = this.transactionId.ifEmpty { java.util.UUID.randomUUID().toString() },
    date = this.date,
    time = this.time,
    type = this.type.label,
    category = this.category,
    amount = this.amount,
    paymentMethod = this.paymentMethod.label,
    notes = this.notes,
    month = this.month,
    weekNo = this.weekNo,
    syncStatus = this.syncStatus,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    version = this.version,
    deleted = this.deleted
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = this.id,
    month = this.month,
    category = this.category,
    budgetAmount = this.budgetAmount,
    syncStatus = this.syncStatus,
    updatedAt = this.updatedAt,
    version = this.version,
    deleted = this.deleted,
    spentSoFar = 0.0 // To be filled by aggregation logic
)

fun Budget.toEntity(budgetIdStr: String? = null): BudgetEntity = BudgetEntity(
    id = this.id,
    budgetId = budgetIdStr ?: java.util.UUID.randomUUID().toString(),
    month = this.month,
    category = this.category,
    budgetAmount = this.budgetAmount,
    syncStatus = this.syncStatus,
    updatedAt = this.updatedAt,
    version = this.version,
    deleted = this.deleted
)

fun CategoryEntity.toDomain(): Category = Category(
    id = this.id,
    name = this.name,
    type = safeEnumParse(this.type, TransactionType.EXPENSE),
    isCustom = this.isCustom,
    isDefault = this.isDefault
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = this.id,
    name = this.name,
    type = this.type.label,
    isCustom = this.isCustom,
    isDefault = this.isDefault
)
