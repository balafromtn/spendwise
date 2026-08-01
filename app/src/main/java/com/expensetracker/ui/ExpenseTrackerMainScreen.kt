package com.expensetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.expensetracker.data.remote.AuthManager
import com.expensetracker.ui.navigation.ExpenseTrackerNavHost

import androidx.compose.runtime.collectAsState

@Composable
fun ExpenseTrackerMainScreen(authManager: AuthManager) {
    val isSignedIn by authManager.isSignedIn.collectAsState()

    ExpenseTrackerNavHost(
        isSignedIn = isSignedIn,
        onSignOut = {
            authManager.signOut()
        }
    )
}
