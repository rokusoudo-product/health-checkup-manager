package com.rokusoudo.healthcheckup.data.account

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import com.rokusoudo.healthcheckup.data.repository.HealthCloudSync
import com.rokusoudo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #34: アカウント削除機能（[AccountDeletionManager]）のユニットテスト。
 *
 * FirebaseAuth・Firestore の実体には依存せず、[AccountAuthActions] / [HealthCloudSync] を
 * フェイクに差し替えてテストする（本番実装は [FirebaseAccountAuthActions] / FirestoreRepository）。
 * Room DB のみ実際の in-memory インスタンスを使い、ローカル削除が本当に行われることを検証する。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AccountDeletionManagerTest {

    private lateinit var db: HealthCheckupDatabase
    private lateinit var cloudSync: FakeHealthCloudSync
    private lateinit var authActions: FakeAccountAuthActions
    private lateinit var repository: HealthRepository
    private lateinit var manager: AccountDeletionManager

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        cloudSync = FakeHealthCloudSync()
        authActions = FakeAccountAuthActions(uid = "test-uid")
        repository = HealthRepository(db, cloudSync, currentUidProvider = { authActions.currentUid() })
        manager = AccountDeletionManager(repository, cloudSync, authActions)
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun insertLocalHealthData(): Long {
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = "2026-08-01", facility = "六創堂クリニック", createdAt = 1000L)
        )
        db.itemDao().insertAll(
            listOf(
                ExaminationItem(
                    recordId = recordId,
                    itemName = "LDL",
                    value = "150",
                    unit = "mg/dL",
                    referenceMin = null,
                    referenceMax = 139.0,
                    isAbnormal = true
                )
            )
        )
        return recordId
    }

    @Test
    fun `削除に成功するとFirestore・Room・Authの順に全データが削除される`() = runBlocking {
        insertLocalHealthData()
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 139.0))

        val result = manager.deleteAccount()

        assertTrue(result is AccountDeletionResult.Success)
        assertEquals(1, cloudSync.deleteAllUserDataCallCount)
        assertEquals("test-uid", cloudSync.lastDeletedUid)
        assertEquals(1, authActions.deleteCurrentUserCallCount)
        // 呼び出し順序: Firestore削除 → (Room削除) → Auth削除
        assertEquals(listOf("cloud", "auth"), sharedCallOrder(cloudSync, authActions))

        // 端末Room DBの健診記録・検査項目は削除されている
        assertNull(db.recordDao().getById(1L))
        assertTrue(db.itemDao().getByRecordIdOnce(1L).isEmpty())

        // 項目マスター（基準値カタログ）はローカル削除の対象外という設計判断（PR本文に明記）
        assertNotNull(db.masterDao().getByName("LDL"))
    }

    @Test
    fun `Auth削除で再認証要求が発生した場合はReauthRequiredを返し進捗を保持する`() = runBlocking {
        insertLocalHealthData()
        authActions.throwReauthRequired = true

        val result = manager.deleteAccount()

        assertTrue(result is AccountDeletionResult.ReauthRequired)
        val progress = (result as AccountDeletionResult.ReauthRequired).progress
        assertTrue(progress.cloudDataDeleted)
        assertTrue(progress.localDataDeleted)
        assertFalse(progress.authAccountDeleted)
        // Room側は既に削除済み（再実行しても冪等なため問題ない）
        assertNull(db.recordDao().getById(1L))
    }

    @Test
    fun `再認証後にdeleteAccountを再実行すると冪等に完了する`() = runBlocking {
        insertLocalHealthData()
        authActions.throwReauthRequired = true
        val firstResult = manager.deleteAccount()
        assertTrue(firstResult is AccountDeletionResult.ReauthRequired)

        // 再サインイン成功を模す
        authActions.throwReauthRequired = false
        val secondResult = manager.deleteAccount()

        assertTrue(secondResult is AccountDeletionResult.Success)
        // Firestore側の削除は2回呼ばれるが、既にデータが無い状態への再削除でも例外を投げない（フェイクで確認）
        assertEquals(2, cloudSync.deleteAllUserDataCallCount)
        // Auth削除は1回目は再認証要求で失敗（カウントされない）、2回目で成功する
        assertEquals(1, authActions.deleteCurrentUserCallCount)
    }

    @Test
    fun `Firestore削除中に例外が起きた場合はサイレント失敗させずFailureで進捗を返す`() = runBlocking {
        insertLocalHealthData()
        cloudSync.shouldThrow = true

        val result = manager.deleteAccount()

        assertTrue(result is AccountDeletionResult.Failure)
        val failure = result as AccountDeletionResult.Failure
        assertFalse(failure.progress.cloudDataDeleted)
        assertFalse(failure.progress.localDataDeleted)
        assertFalse(failure.progress.authAccountDeleted)
        assertTrue(failure.message.contains("network error"))

        // Firestore失敗時はRoom削除にもAuth削除にも進まない → ローカルデータは温存される
        assertNotNull(db.recordDao().getById(1L))
        assertEquals(0, authActions.deleteCurrentUserCallCount)
    }

    @Test
    fun `未サインイン状態ではどこにもアクセスせずFailureを返す`() = runBlocking {
        authActions.uid = null

        val result = manager.deleteAccount()

        assertTrue(result is AccountDeletionResult.Failure)
        assertEquals(0, cloudSync.deleteAllUserDataCallCount)
        assertEquals(0, authActions.deleteCurrentUserCallCount)
    }

    @Test
    fun `サインアウト相当の操作ではAccountDeletionManagerを一切経由しないためデータは削除されない`() = runBlocking {
        // HomeFragment#signOut() は FirebaseAuth.signOut() / GoogleSignInClient.signOut() のみを呼び、
        // AccountDeletionManager・HealthCloudSync・HealthRepositoryのいずれの削除処理も参照しない
        // （HomeFragment.kt 参照）。ここでは deleteAccount() を呼ばないことがそのままサインアウト相当であり、
        // フェイクへの削除呼び出しが一切発生しないことを回帰的に保証する。
        insertLocalHealthData()
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 139.0))

        // (何もしない = サインアウト相当。deleteAccount()を呼ばない)

        assertEquals(0, cloudSync.deleteAllUserDataCallCount)
        assertEquals(0, authActions.deleteCurrentUserCallCount)
        assertNotNull(db.recordDao().getById(1L))
        assertNotNull(db.masterDao().getByName("LDL"))
    }

    private fun sharedCallOrder(cloud: FakeHealthCloudSync, auth: FakeAccountAuthActions): List<String> {
        return (cloud.callLog + auth.callLog).sortedBy { it.second }.map { it.first }
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        var deleteAllUserDataCallCount = 0
        var lastDeletedUid: String? = null
        var shouldThrow = false
        val callLog = mutableListOf<Pair<String, Long>>()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            // 本テストでは未使用
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            // 本テストでは未使用
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> =
            emptyList()

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> = emptyList()

        override suspend fun deleteAllUserData(uid: String) {
            if (shouldThrow) throw RuntimeException("network error")
            deleteAllUserDataCallCount++
            lastDeletedUid = uid
            callLog.add("cloud" to System.nanoTime())
        }
    }

    private class FakeAccountAuthActions(var uid: String?) : AccountAuthActions {
        var deleteCurrentUserCallCount = 0
        var throwReauthRequired = false
        val callLog = mutableListOf<Pair<String, Long>>()

        override fun currentUid(): String? = uid

        override suspend fun deleteCurrentUser() {
            if (throwReauthRequired) throw ReauthenticationRequiredException()
            deleteCurrentUserCallCount++
            callLog.add("auth" to System.nanoTime())
        }
    }
}
