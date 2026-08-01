package com.expensetracker.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(private val context: Context, private val webClientId: String = "") {

    private val gso: GoogleSignInOptions by lazy {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(TokenProvider.SPREADSHEETS_SCOPE),
                Scope(TokenProvider.DRIVE_FILE_SCOPE)
            )
        if (webClientId.isNotBlank()) {
            builder.requestIdToken(webClientId)
        }
        builder.build()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    private val _isSignedIn = MutableStateFlow(GoogleSignIn.getLastSignedInAccount(context) != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    val signInIntent get() = googleSignInClient.signInIntent

    fun getSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)

    fun signOut(): Task<Void> = googleSignInClient.signOut().addOnCompleteListener {
        updateSignInState()
    }

    fun updateSignInState() {
        _isSignedIn.value = GoogleSignIn.getLastSignedInAccount(context) != null
    }
}
