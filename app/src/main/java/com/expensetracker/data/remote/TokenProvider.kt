package com.expensetracker.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets

class TokenProvider(
    private val context: Context,
    private val authManager: AuthManager
) {
    companion object {
        const val SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets"
        const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"
    }

    fun getSheetsService(): Sheets? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(SPREADSHEETS_SCOPE, DRIVE_FILE_SCOPE)
        ).apply {
            selectedAccount = account.account
        }
        return Sheets.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Spendwise")
            .build()
    }

    fun isAuthorized(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getUserEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }
}
