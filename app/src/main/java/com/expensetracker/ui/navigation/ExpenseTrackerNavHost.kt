package com.expensetracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.expensetracker.ui.theme.LocalBrandGradient
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expensetracker.ui.auth.AuthScreen
import com.expensetracker.ui.categories.CategoriesScreen
import com.expensetracker.ui.dashboard.DashboardScreen
import com.expensetracker.ui.settings.SettingsScreen
import com.expensetracker.ui.transaction.AddTransactionScreen
import com.expensetracker.ui.transaction.TransactionListScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ExpenseTrackerNavHost(
    isSignedIn: Boolean,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val startDestination = if (isSignedIn) Screen.Dashboard.route else Screen.Auth.route

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Dashboard.route),
        BottomNavItem("Transactions", Icons.Default.List, Screen.TransactionList.route),
        BottomNavItem("Analytics", Icons.Default.BarChart, Screen.Analytics.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddTransaction.route) {
                            launchSingleTop = true
                        }
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier.background(
                        LocalBrandGradient.current,
                        FloatingActionButtonDefaults.shape
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.TransactionList.route) {
                TransactionListScreen(
                    onEditTransaction = { transactionId ->
                        navController.navigate("edit_transaction/$transactionId")
                    }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId")
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    editTransactionId = transactionId
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen()
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSignOut = onSignOut,
                    onNavigateBack = { navController.popBackStack() },
                    onManageCategories = {
                        navController.navigate(Screen.Categories.route)
                    }
                )
            }
        }
    }
}
