package com.expensetracker.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object TransactionList : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}")
    object Categories : Screen("categories")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
}
