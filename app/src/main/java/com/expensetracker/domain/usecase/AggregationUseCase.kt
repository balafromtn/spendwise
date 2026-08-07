package com.expensetracker.domain.usecase

import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.local.dao.CategoryTotal
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.CategoryBreakdown
import com.expensetracker.domain.model.MonthlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AggregationUseCase(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun getMonthlySummary(month: String): Flow<MonthlySummary> {
        return combine(
            transactionDao.getTotalIncomeByMonth(month),
            transactionDao.getTotalExpenseByMonth(month),
            transactionDao.getTransactionsByMonth(month),
            transactionDao.getCategoryTotals("Income", month),
            transactionDao.getCategoryTotals("Expense", month),
            transactionDao.getHighestAmount("Income", month),
            transactionDao.getLowestAmount("Income", month),
            transactionDao.getHighestAmount("Expense", month),
            transactionDao.getLowestAmount("Expense", month)
        ) { results ->
            val totalIncome = (results[0] as? Double) ?: 0.0
            val totalExpense = (results[1] as? Double) ?: 0.0
            val transactions = results[2] as? List<TransactionEntity> ?: emptyList()
            val incomeCategoryTotals = results[3] as? List<CategoryTotal> ?: emptyList()
            val expenseCategoryTotals = results[4] as? List<CategoryTotal> ?: emptyList()
            val highestIncome = (results[5] as? Double) ?: 0.0
            val lowestIncome = (results[6] as? Double) ?: 0.0
            val highestExpense = (results[7] as? Double) ?: 0.0
            val lowestExpense = (results[8] as? Double) ?: 0.0

            val incomeBreakdown = incomeCategoryTotals.map { cat ->
                CategoryBreakdown(
                    category = cat.category,
                    amount = cat.total,
                    percentage = if (totalIncome > 0) (cat.total / totalIncome) * 100 else 0.0,
                    type = com.expensetracker.domain.model.TransactionType.INCOME
                )
            }

            val expenseBreakdown = expenseCategoryTotals.map { cat ->
                CategoryBreakdown(
                    category = cat.category,
                    amount = cat.total,
                    percentage = if (totalExpense > 0) (cat.total / totalExpense) * 100 else 0.0,
                    type = com.expensetracker.domain.model.TransactionType.EXPENSE
                )
            }

            val dateUtils = DateUtils()
            val monthDate = try {
                java.time.LocalDate.parse("01-$month", java.time.format.DateTimeFormatter.ofPattern("dd-MMM yyyy"))
            } catch (e: Exception) {
                java.time.LocalDate.now()
            }
            val weeksElapsed = dateUtils.weeksElapsedInMonth(
                if (monthDate.month == java.time.LocalDate.now().month && monthDate.year == java.time.LocalDate.now().year) {
                    java.time.LocalDate.now() // Current month: use today for accurate "weeks so far"
                } else {
                    monthDate.withDayOfMonth(monthDate.lengthOfMonth()) // Past month: use last day
                }
            )
            val avgWeekly = if (weeksElapsed > 0) totalExpense / weeksElapsed else 0.0

            MonthlySummary(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netSavings = totalIncome - totalExpense,
                transactionCount = transactions.size,
                highestIncome = highestIncome,
                lowestIncome = lowestIncome,
                highestExpense = highestExpense,
                lowestExpense = lowestExpense,
                categoryBreakdown = incomeBreakdown + expenseBreakdown,
                averageWeeklySpend = avgWeekly
            )
        }
    }

    fun getBudgetsWithSpending(month: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(month).combine(
            transactionDao.getCategoryTotals("Expense", month)
        ) { budgets, categoryTotals ->
            val spentMap = categoryTotals.associate { it.category to it.total }
            budgets.map { budget ->
                Budget(
                    id = budget.id,
                    month = budget.month,
                    category = budget.category,
                    budgetAmount = budget.budgetAmount,
                    spentSoFar = spentMap[budget.category] ?: 0.0,
                    syncStatus = budget.syncStatus,
                    updatedAt = budget.updatedAt,
                    version = budget.version,
                    deleted = budget.deleted
                )
            }
        }
    }

    fun getCategoryBreakdown(type: String, month: String): Flow<List<CategoryBreakdown>> {
        val totalFlow = if (type == "Income") {
            transactionDao.getTotalIncomeByMonth(month)
        } else {
            transactionDao.getTotalExpenseByMonth(month)
        }
        val enumType = try { com.expensetracker.domain.model.TransactionType.valueOf(type.uppercase()) } catch (e: Exception) { com.expensetracker.domain.model.TransactionType.EXPENSE }
        return totalFlow.combine(transactionDao.getCategoryTotals(type, month)) { total, cats ->
            cats.map { cat ->
                CategoryBreakdown(
                    category = cat.category,
                    amount = cat.total,
                    percentage = if ((total ?: 0.0) > 0) (cat.total / (total ?: 1.0)) * 100 else 0.0,
                    type = enumType
                )
            }
        }
    }
}
