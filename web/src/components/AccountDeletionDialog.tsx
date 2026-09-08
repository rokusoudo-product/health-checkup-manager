import { useState } from 'react'
import { reauthenticateWithPopup } from 'firebase/auth'
import { auth, googleProvider } from '../firebase'
import { deleteAllUserData } from '../firestoreService'
import { createFirebaseAccountAuthActions } from '../firebaseAuthActions'
import { deleteAccount } from '../accountDeletion'
import type { AccountDeletionProgress, AccountDeletionResult } from '../accountDeletion'

type Phase = 'confirm' | 'in-progress' | 'reauth-required' | 'error'

interface Props {
  /** モーダルを閉じる（削除成功時も呼ぶ。成功後は onAuthStateChanged が自動でログイン画面へ切り替える） */
  onClose: () => void
}

const authActions = createFirebaseAccountAuthActions()

function statusLabel(done: boolean): string {
  return done ? '削除済み' : '未削除'
}

/**
 * Issue #34: Web版アカウント削除モーダル。
 * Android版のホームメニュー「アカウントとデータを削除」と同等の導線・確認内容・処理順序・
 * 再認証フロー・エラー表示方針を、Webの技術スタック（React + Firebase Web SDK）で実装する。
 */
export default function AccountDeletionDialog({ onClose }: Props) {
  const [phase, setPhase] = useState<Phase>('confirm')
  const [progress, setProgress] = useState<AccountDeletionProgress>({
    cloudDataDeleted: false,
    authAccountDeleted: false,
  })
  const [errorMessage, setErrorMessage] = useState('')

  const applyResult = (result: AccountDeletionResult) => {
    if (result.status === 'success') {
      // Firebase Authのユーザーが削除されると onAuthStateChanged が発火し、
      // App.tsx が自動的にログイン画面へ切り替える（ログアウトと同じ仕組み。手動ナビゲーション不要）。
      onClose()
      return
    }
    if (result.status === 'reauth-required') {
      setProgress(result.progress)
      setPhase('reauth-required')
      return
    }
    setProgress(result.progress)
    setErrorMessage(result.message)
    setPhase('error')
  }

  const runDeletion = async () => {
    setPhase('in-progress')
    const result = await deleteAccount({ deleteAllUserData, authActions })
    applyResult(result)
  }

  const runReauthThenRetry = async () => {
    setPhase('in-progress')
    try {
      const user = auth.currentUser
      if (!user) {
        setErrorMessage('サインインしていません')
        setPhase('error')
        return
      }
      await reauthenticateWithPopup(user, googleProvider)
      applyResult(await deleteAccount({ deleteAllUserData, authActions }))
    } catch (e) {
      setErrorMessage(e instanceof Error ? e.message : '再認証に失敗しました')
      setPhase('error')
    }
  }

  const closable = phase !== 'in-progress'

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={closable ? onClose : undefined}
    >
      <div
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="account-deletion-title"
        onClick={(e) => e.stopPropagation()}
      >
        {phase === 'confirm' && (
          <>
            <h2 id="account-deletion-title">アカウントとデータを削除しますか？</h2>
            <p className="modal-message">
              以下のデータがすべて削除されます。この操作は取り消せません。
            </p>
            <ul className="modal-list">
              <li>クラウド上の全健診記録</li>
              <li>ログイン情報（アカウント）</li>
            </ul>
            <div className="modal-actions">
              {/* 誤操作防止のため既定フォーカスはキャンセル側（Android版と同じ方針） */}
              <button className="btn-secondary" onClick={onClose} autoFocus>キャンセル</button>
              <button className="btn-delete-account" onClick={runDeletion}>削除する</button>
            </div>
          </>
        )}

        {phase === 'in-progress' && (
          <>
            <h2 id="account-deletion-title">削除しています…</h2>
            <p className="modal-message">しばらくお待ちください。</p>
          </>
        )}

        {phase === 'reauth-required' && (
          <>
            <h2 id="account-deletion-title">再度サインインしてください</h2>
            <p className="modal-message">
              セキュリティのため、削除を完了するには最近のサインインが必要です。
              Googleで再サインインしてから削除を続けます。
            </p>
            <div className="modal-actions">
              <button className="btn-secondary" onClick={onClose}>キャンセル</button>
              <button className="btn-delete-account" onClick={runReauthThenRetry}>
                再サインインして続ける
              </button>
            </div>
          </>
        )}

        {phase === 'error' && (
          <>
            <h2 id="account-deletion-title">削除を完了できませんでした</h2>
            <p className="error">エラーが発生しました: {errorMessage}</p>
            <p className="modal-message">現在の状況:</p>
            <ul className="modal-list">
              <li>クラウド上のデータ: {statusLabel(progress.cloudDataDeleted)}</li>
              <li>アカウント本体: {statusLabel(progress.authAccountDeleted)}</li>
            </ul>
            <div className="modal-actions">
              <button className="btn-secondary" onClick={onClose}>閉じる</button>
              <button className="btn-delete-account" onClick={runDeletion}>再試行</button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
