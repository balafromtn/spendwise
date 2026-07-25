package com.expensetracker.ui.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.remote.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                Log.d("AuthViewModel", "Processing sign-in result...")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                Log.d("AuthViewModel", "Sign-in successful: ${account?.email}")
                AuthManager.updateSignInState()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignedIn = true
                )
            } catch (e: ApiException) {
                Log.e("AuthViewModel", "Google Sign-In API error: code=${e.statusCode}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Google Sign-In failed (code ${e.statusCode}). Check your SHA-1 fingerprint and OAuth client config."
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign-in failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sign in failed: ${e.message}"
                )
            }
        }
    }
}
