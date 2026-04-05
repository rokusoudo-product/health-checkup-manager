package com.rokusodo.healthcheckup.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rokusodo.healthcheckup.HealthCheckupApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ログイン画面の ViewModel。
 * Firebase Auth を使った Google SSO の状態を管理する。
 * ログイン成功後、Firestoreから既存データをRoomへ復元する（T-403）。
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = (application as HealthCheckupApp).repository

    /**
     * Google ID トークンを使って Firebase Auth でサインインする。
     * サインイン成功後、Firestoreから既存データをRoomへ復元する。
     */
    fun signIn(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()

                // Firestore → Room 復元（他端末で入力したデータをローカルに同期）
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    repository.restoreFromFirestore(uid)
                }

                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
