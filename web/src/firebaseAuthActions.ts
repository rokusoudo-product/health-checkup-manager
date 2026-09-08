import { FirebaseError } from 'firebase/app'
import { deleteUser } from 'firebase/auth'
import { auth } from './firebase'
import type { AccountAuthActions } from './accountDeletion'
import { ReauthenticationRequiredError } from './accountDeletion'

/**
 * [AccountAuthActions] の本番実装。Firebase Auth を直接操作する。
 * `auth/requires-recent-login` エラーはここでのみキャッチし、
 * テスト可能な ReauthenticationRequiredError に変換して上位へ伝える。
 */
export function createFirebaseAccountAuthActions(): AccountAuthActions {
  return {
    currentUid: () => auth.currentUser?.uid ?? null,
    deleteCurrentUser: async () => {
      const user = auth.currentUser
      if (!user) throw new Error('サインインしていません')
      try {
        await deleteUser(user)
      } catch (e) {
        if (e instanceof FirebaseError && e.code === 'auth/requires-recent-login') {
          throw new ReauthenticationRequiredError(e)
        }
        throw e
      }
    },
  }
}
