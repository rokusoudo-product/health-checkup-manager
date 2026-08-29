import { describe, expect, it } from 'vitest'
import {
  ReauthenticationRequiredError,
  deleteAccount,
} from './accountDeletion'
import type { AccountAuthActions, AccountDeletionDeps } from './accountDeletion'

/**
 * Issue #34: アカウント削除機能（Web版）のユニットテスト。
 * Firebase SDKには一切依存せず、AccountAuthActions・deleteAllUserData をフェイクに差し替えてテストする
 * （Android版 AccountDeletionManagerTest と同じ設計意図）。
 */

function makeAuthActions(overrides: Partial<AccountAuthActions> = {}): AccountAuthActions & {
  deleteCurrentUserCallCount: number
} {
  const state = { deleteCurrentUserCallCount: 0 }
  return {
    currentUid: overrides.currentUid ?? (() => 'test-uid'),
    deleteCurrentUser: overrides.deleteCurrentUser
      ?? (async () => {
        state.deleteCurrentUserCallCount++
      }),
    get deleteCurrentUserCallCount() {
      return state.deleteCurrentUserCallCount
    },
  }
}

describe('deleteAccount', () => {
  it('成功時はFirestore削除→Auth削除の順に実行され成功を返す', async () => {
    const callOrder: string[] = []
    const authActions = makeAuthActions({
      deleteCurrentUser: async () => {
        callOrder.push('auth')
      },
    })
    const deleteAllUserData = async (uid: string) => {
      expect(uid).toBe('test-uid')
      callOrder.push('cloud')
    }
    const deps: AccountDeletionDeps = { deleteAllUserData, authActions }

    const result = await deleteAccount(deps)

    expect(result).toEqual({ status: 'success' })
    expect(callOrder).toEqual(['cloud', 'auth'])
  })

  it('Auth削除で再認証が必要な場合はreauth-requiredを返し進捗を保持する', async () => {
    const authActions = makeAuthActions({
      deleteCurrentUser: async () => {
        throw new ReauthenticationRequiredError()
      },
    })
    const deleteAllUserData = async () => {}
    const result = await deleteAccount({ deleteAllUserData, authActions })

    expect(result.status).toBe('reauth-required')
    if (result.status === 'reauth-required') {
      expect(result.progress).toEqual({ cloudDataDeleted: true, authAccountDeleted: false })
    }
  })

  it('再認証後にdeleteAccountを再実行すると冪等に完了する', async () => {
    let shouldThrow = true
    let authCallCount = 0
    let cloudCallCount = 0
    const authActions = makeAuthActions({
      deleteCurrentUser: async () => {
        authCallCount++
        if (shouldThrow) throw new ReauthenticationRequiredError()
      },
    })
    const deleteAllUserData = async () => {
      cloudCallCount++
    }
    const deps: AccountDeletionDeps = { deleteAllUserData, authActions }

    const first = await deleteAccount(deps)
    expect(first.status).toBe('reauth-required')

    shouldThrow = false
    const second = await deleteAccount(deps)

    expect(second).toEqual({ status: 'success' })
    expect(cloudCallCount).toBe(2)
    expect(authCallCount).toBe(2)
  })

  it('Firestore削除中に例外が起きた場合はサイレント失敗させずerrorで進捗を返す', async () => {
    const authActions = makeAuthActions()
    const deleteAllUserData = async () => {
      throw new Error('network error')
    }

    const result = await deleteAccount({ deleteAllUserData, authActions })

    expect(result.status).toBe('error')
    if (result.status === 'error') {
      expect(result.progress).toEqual({ cloudDataDeleted: false, authAccountDeleted: false })
      expect(result.message).toBe('network error')
    }
    expect(authActions.deleteCurrentUserCallCount).toBe(0)
  })

  it('未サインイン状態ではどこにもアクセスせずerrorを返す', async () => {
    let deleteAllUserDataCallCount = 0
    const authActions = makeAuthActions({ currentUid: () => null })
    const deleteAllUserData = async () => {
      deleteAllUserDataCallCount++
    }

    const result = await deleteAccount({ deleteAllUserData, authActions })

    expect(result.status).toBe('error')
    expect(deleteAllUserDataCallCount).toBe(0)
    expect(authActions.deleteCurrentUserCallCount).toBe(0)
  })
})
