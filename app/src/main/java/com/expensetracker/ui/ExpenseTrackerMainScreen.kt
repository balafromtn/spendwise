package com.expensetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.expensetracker.data.remote.AuthManager
import com.expensetracker.ui.navigation.ExpenseTrackerNavHost

@Composable
fun ExpenseTrackerMainScreen() {
    var isSignedIn by remember { mutableStateOf(false) }
    var authChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isSignedIn = AuthManager.isSignedInGlobal
        authChecked = true
    }

    if (authChecked) {
        ExpenseTrackerNavHost(
            isSignedIn = isSignedIn,
            onSignOut = {
                isSignedIn = false
            }
        )
    }
}
