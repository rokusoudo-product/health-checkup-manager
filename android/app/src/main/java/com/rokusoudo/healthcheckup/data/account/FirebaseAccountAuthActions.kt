package com.rokusoudo.healthcheckup.data.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.tasks.await

/**
 * [AccountAuthActions] の本番実装。FirebaseAuth を直接操作する。
 * `FirebaseAuthRecentLoginRequiredException` はここでのみキャッチし、
 * テスト可能な [ReauthenticationRequiredException] に変換して上位へ伝える。
 */
class FirebaseAccountAuthActions(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AccountAuthActions {

    override fun currentUid(): String? = auth.currentUser?.uid

    override suspend fun deleteCurrentUser() {
        val user = auth.currentUser ?: throw IllegalStateException("サインインしていません")
        try {
            user.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw ReauthenticationRequiredException(e)
        }
    }
}
