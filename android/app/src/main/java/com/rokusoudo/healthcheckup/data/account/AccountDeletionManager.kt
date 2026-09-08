package com.rokusoudo.healthcheckup.data.account

import com.rokusoudo.healthcheckup.data.repository.HealthCloudSync
import com.rokusoudo.healthcheckup.data.repository.HealthRepository

/**
 * Issue #34: アカウント削除機能。
 *
 * FirebaseAuth のようなfinalクラス・パッケージプライベートな例外（
 * [com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException] はコンストラクタが公開されておらず
 * テストで直接生成できない）に依存すると、この画面のロジックをユニットテストできなくなる。
 * そのため実際の認証操作は [AccountAuthActions] インターフェースの背後に隠し、
 * 再認証が必要な状況は自前の [ReauthenticationRequiredException] として表現する。
 * 実装は [com.rokusoudo.healthcheckup.data.account.FirebaseAccountAuthActions]。
 */
interface AccountAuthActions {
    /** ログイン中ユーザーの uid。未ログインなら null。 */
    fun currentUid(): String?

    /**
     * ログイン中の Firebase Auth ユーザーを削除する。
     * 直近のログインから時間が経っている場合、実装は [ReauthenticationRequiredException] を投げること。
     */
    suspend fun deleteCurrentUser()
}

/**
 * Firebase Auth の再認証要求（`FirebaseAuthRecentLoginRequiredException` 相当）を表す。
 * ユニットテストで自由に生成できるよう、実際の Firebase 例外をラップする独自の例外型にしている。
 */
class ReauthenticationRequiredException(cause: Throwable? = null) : Exception(cause)

/**
 * アカウント削除処理のうち、どこまで完了したかを表す。
 * 途中失敗時に「何が削除され、何が残ったか」を利用者に伝えるために使う（Issue #34 受け入れ基準）。
 */
data class AccountDeletionProgress(
    val cloudDataDeleted: Boolean = false,
    val localDataDeleted: Boolean = false,
    val authAccountDeleted: Boolean = false
)

sealed class AccountDeletionResult {
    object Success : AccountDeletionResult()
    data class ReauthRequired(val progress: AccountDeletionProgress) : AccountDeletionResult()
    data class Failure(val progress: AccountDeletionProgress, val message: String) : AccountDeletionResult()
}

/**
 * アカウント・データ削除を決められた順序で実行するユースケース。
 *
 * 削除順序（Issue #34 の受け入れ基準どおり）:
 *   1. Firestore の users/{uid} 配下（記録・項目マスター）
 *   2. 端末 Room DB の健診記録（記録・検査項目）
 *   3. Firebase Auth のユーザー本体
 *
 * 途中で失敗しても [deleteAccount] を再実行すれば安全なよう、各ステップは冪等に作られている
 * （Firestore側の各delete系メソッド・Room DBのDELETEクエリは、対象が既に無くてもエラーにならない）。
 * 再認証要求（[ReauthenticationRequiredException]）を検知した場合は、それまでの進捗を保持したまま
 * [AccountDeletionResult.ReauthRequired] を返す。呼び出し側が再認証後に [deleteAccount] を呼び直すと、
 * 既に完了済みのステップは実質no-opとなり、Auth削除のみが新たに実行される。
 */
class AccountDeletionManager(
    private val repository: HealthRepository,
    private val cloudSync: HealthCloudSync,
    private val authActions: AccountAuthActions
) {
    suspend fun deleteAccount(): AccountDeletionResult {
        val uid = authActions.currentUid()
            ?: return AccountDeletionResult.Failure(
                AccountDeletionProgress(),
                "サインインしていません"
            )

        var progress = AccountDeletionProgress()
        return try {
            cloudSync.deleteAllUserData(uid)
            progress = progress.copy(cloudDataDeleted = true)

            repository.deleteAllLocalHealthData()
            progress = progress.copy(localDataDeleted = true)

            authActions.deleteCurrentUser()
            progress = progress.copy(authAccountDeleted = true)

            AccountDeletionResult.Success
        } catch (e: ReauthenticationRequiredException) {
            AccountDeletionResult.ReauthRequired(progress)
        } catch (e: Exception) {
            AccountDeletionResult.Failure(progress, e.message ?: e.javaClass.simpleName)
        }
    }
}
