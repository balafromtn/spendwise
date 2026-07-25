package com.expensetracker.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task

class AuthManager(private val context: Context) {

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(TokenProvider.SPREADSHEETS_SCOPE))
            .requestIdToken(webClientId)
            .build()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    val signInIntent get() = googleSignInClient.signInIntent

    fun getSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)

    fun signOut(): Task<Void> = googleSignInClient.signOut()

    fun isSignedIn(): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

    companion object {
        lateinit var webClientId: String
            private set
        private var appContext: Context? = null

        var isSignedInGlobal: Boolean = false
            private set

        fun init(clientId: String) {
            webClientId = clientId
        }

        fun setContext(context: Context) {
            appContext = context.applicationContext
        }

        fun updateSignInState() {
            appContext?.let { ctx ->
                isSignedInGlobal = GoogleSignIn.getLastSignedInAccount(ctx) != null
            }
        }
    }
}
