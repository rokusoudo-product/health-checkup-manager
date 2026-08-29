/**
 * Issue #34: アカウント削除機能（Web版）。
 *
 * Android版の AccountDeletionManager / AccountAuthActions と同じ設計:
 * Firebase Auth の実操作は [AccountAuthActions] インターフェースの背後に隠し、
 * 再認証が必要な状況は自前の [ReauthenticationRequiredError] として表現する。
 * これにより、Firebase SDKを一切importせずにこのモジュール単体をユニットテストできる。
 * 本番実装は firebaseAuthActions.ts の createFirebaseAccountAuthActions。
 */

export class ReauthenticationRequiredError extends Error {
  constructor(cause?: unknown) {
    super('reauthentication required')
    this.name = 'ReauthenticationRequiredError'
    if (cause !== undefined) {
      // Error.cause はモダンブラウザ/TSでサポートされているが、型定義互換のため明示的に代入する
      ;(this as unknown as { cause?: unknown }).cause = cause
    }
  }
}

export interface AccountAuthActions {
  /** ログイン中ユーザーの uid。未ログインなら null。 */
  currentUid: () => string | null
  /**
   * ログイン中の Firebase Auth ユーザーを削除する。
   * 直近のログインから時間が経っている場合、実装は ReauthenticationRequiredError を投げること。
   */
  deleteCurrentUser: () => Promise<void>
}

/**
 * アカウント削除処理のうち、どこまで完了したかを表す。
 * 途中失敗時に「何が削除され、何が残ったか」を利用者に伝えるために使う（Issue #34 受け入れ基準）。
 */
export interface AccountDeletionProgress {
  cloudDataDeleted: boolean
  authAccountDeleted: boolean
}

export type AccountDeletionResult =
  | { status: 'success' }
  | { status: 'reauth-required'; progress: AccountDeletionProgress }
  | { status: 'error'; progress: AccountDeletionProgress; message: string }

export interface AccountDeletionDeps {
  deleteAllUserData: (uid: string) => Promise<void>
  authActions: AccountAuthActions
}

/**
 * アカウント・データ削除を決められた順序で実行するユースケース。
 *
 * 削除順序（Issue #34 の受け入れ基準どおり。Web版はRoom DB相当のローカル永続化を持たないため対象外）:
 *   1. Firestore の users/{uid} 配下（記録・項目マスター）
 *   2. Firebase Auth のユーザー本体
 *
 * 各ステップは冪等（対象が既に無くてもエラーにならない）。再認証要求を検知した場合は
 * それまでの進捗を保持したまま 'reauth-required' を返す。呼び出し側が再認証後に
 * deleteAccount を呼び直すと、既に完了済みのFirestore削除は実質no-opとなり、
 * Auth削除のみが新たに実行される。
 */
export async function deleteAccount(deps: AccountDeletionDeps): Promise<AccountDeletionResult> {
  const uid = deps.authActions.currentUid()
  if (uid === null) {
    return {
      status: 'error',
      progress: { cloudDataDeleted: false, authAccountDeleted: false },
      message: 'サインインしていません',
    }
  }

  let progress: AccountDeletionProgress = { cloudDataDeleted: false, authAccountDeleted: false }
  try {
    await deps.deleteAllUserData(uid)
    progress = { ...progress, cloudDataDeleted: true }

    await deps.authActions.deleteCurrentUser()
    progress = { ...progress, authAccountDeleted: true }

    return { status: 'success' }
  } catch (e) {
    if (e instanceof ReauthenticationRequiredError) {
      return { status: 'reauth-required', progress }
    }
    return {
      status: 'error',
      progress,
      message: e instanceof Error ? e.message : '不明なエラーが発生しました',
    }
  }
}
