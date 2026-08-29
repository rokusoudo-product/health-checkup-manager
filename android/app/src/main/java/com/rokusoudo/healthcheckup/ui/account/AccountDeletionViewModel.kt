package com.rokusoudo.healthcheckup.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rokusoudo.healthcheckup.HealthCheckupApp
import com.rokusoudo.healthcheckup.data.account.AccountDeletionManager
import com.rokusoudo.healthcheckup.data.account.AccountDeletionProgress
import com.rokusoudo.healthcheckup.data.account.AccountDeletionResult
import com.rokusoudo.healthcheckup.data.account.FirebaseAccountAuthActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Issue #34: 「アカウントとデータを削除」画面（ホームのオーバーフローメニューから遷移）の ViewModel。
 * 削除処理の進行状況を [DeletionUiState] として公開する。
 */
class AccountDeletionViewModel(application: Application) : AndroidViewModel(application) {

    sealed class DeletionUiState {
        object Idle : DeletionUiState()
        object InProgress : DeletionUiState()
        object Success : DeletionUiState()
        data class ReauthRequired(val progress: AccountDeletionProgress) : DeletionUiState()
        data class Error(val progress: AccountDeletionProgress, val message: String) : DeletionUiState()
    }

    private val _uiState = MutableStateFlow<DeletionUiState>(DeletionUiState.Idle)
    val uiState: StateFlow<DeletionUiState> = _uiState.asStateFlow()

    private val app = application as HealthCheckupApp
    private val manager = AccountDeletionManager(
        repository = app.repository,
        cloudSync = app.firestoreRepository,
        authActions = FirebaseAccountAuthActions()
    )

    /** 確認ダイアログで「削除する」が選択されたときに呼ぶ。 */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = DeletionUiState.InProgress
            applyResult(manager.deleteAccount())
        }
    }

    /**
     * 再認証エラー後、Google再サインインで取得したIDトークンで再認証してから削除を再試行する。
     * 再認証に成功した場合、既に完了済みの削除ステップは冪等なので安全にやり直せる。
     */
    fun reauthenticateThenRetry(idToken: String) {
        viewModelScope.launch {
            _uiState.value = DeletionUiState.InProgress
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    _uiState.value = DeletionUiState.Error(AccountDeletionProgress(), "サインインしていません")
                    return@launch
                }
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                user.reauthenticate(credential).await()
                applyResult(manager.deleteAccount())
            } catch (e: Exception) {
                _uiState.value = DeletionUiState.Error(
                    AccountDeletionProgress(),
                    e.message ?: "再認証に失敗しました"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = DeletionUiState.Idle
    }

    private fun applyResult(result: AccountDeletionResult) {
        _uiState.value = when (result) {
            is AccountDeletionResult.Success -> DeletionUiState.Success
            is AccountDeletionResult.ReauthRequired -> DeletionUiState.ReauthRequired(result.progress)
            is AccountDeletionResult.Failure -> DeletionUiState.Error(result.progress, result.message)
        }
    }
}
