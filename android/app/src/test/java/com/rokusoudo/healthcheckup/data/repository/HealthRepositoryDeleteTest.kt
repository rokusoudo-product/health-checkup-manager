package com.rokusoudo.healthcheckup.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #47: 診断記録1件の削除（[HealthRepository.deleteRecord]）を検証する。
 *
 * 設計判断（PR本文にも記載）:
 * - 物理削除。Undo・ゴミ箱は実装しない
 * - Firestore への削除を先に試み、成功した場合のみ Room からも削除する
 * - Firestore 削除が失敗した場合は Room からは削除せず、[Result.failure] を返す
 *   （Issue #46 方式Aの差分ミラー同期によって、Firestore に残ったドキュメントが
 *   次回同期でRoomへ復活する経路を作らないため）
 * - 未ログイン（uidが取れない）場合はFirestore同期の対象外として、Roomのみ削除する
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositoryDeleteTest {

    private lateinit var db: HealthCheckupDatabase
    private lateinit var cloudSync: FakeHealthCloudSync

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        cloudSync = FakeHealthCloudSync()
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun insertRecordWithItems(): Long {
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = "2026-08-01", facility = "六創堂クリニック", createdAt = 1000L)
        )
        db.itemDao().insertAll(
            listOf(
                ExaminationItem(
                    recordId = recordId,
                    itemName = "LDLコレステロール",
                    value = "150",
                    unit = "mg/dL",
                    referenceMin = null,
                    referenceMax = 139.0,
                    isAbnormal = true
                ),
                ExaminationItem(
                    recordId = recordId,
                    itemName = "体重",
                    value = "68.5",
                    unit = "kg",
                    referenceMin = null,
                    referenceMax = null,
                    isAbnormal = false
                )
            )
        )
        return recordId
    }

    @Test
    fun `Firestore削除が成功すると記録と検査項目がRoomから削除される`() = runBlocking {
        val recordId = insertRecordWithItems()
        val remoteId = db.recordDao().getById(recordId)!!.remoteId
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        val result = repository.deleteRecord(recordId)

        assertTrue(result.isSuccess)
        assertNull(db.recordDao().getById(recordId))
        // 孤児レコードが残らないこと（examination_items）
        assertTrue(db.itemDao().getByRecordIdOnce(recordId).isEmpty())
        // Issue #49: Firestoreへの削除呼び出しはRoomのローカルidではなくremoteId（ドキュメントID）で行われる
        assertEquals(listOf(remoteId), cloudSync.deletedRemoteIds)
    }

    @Test
    fun `Firestore削除が失敗した場合はRoomから削除されずエラーが返る`() = runBlocking {
        val recordId = insertRecordWithItems()
        cloudSync.shouldThrowOnDelete = true
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        val result = repository.deleteRecord(recordId)

        assertTrue(result.isFailure)
        // Firestore削除に失敗した場合、Roomの記録・検査項目は残る
        // （先にRoomだけ消すと、Issue #46差分ミラー同期でFirestore側に残った
        // ドキュメントが次回fetchでRoomへ再upsertされ、削除した記録が復活し得るため）
        assertNotNull(db.recordDao().getById(recordId))
        assertEquals(2, db.itemDao().getByRecordIdOnce(recordId).size)
    }

    @Test
    fun `未ログインの場合はFirestoreを呼び出さずRoomから削除される`() = runBlocking {
        val recordId = insertRecordWithItems()
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { null })

        val result = repository.deleteRecord(recordId)

        assertTrue(result.isSuccess)
        assertNull(db.recordDao().getById(recordId))
        assertTrue(cloudSync.deletedRemoteIds.isEmpty())
    }

    @Test
    fun `存在しない記録IDを指定しても例外にならない`() = runBlocking {
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        val result = repository.deleteRecord(999L)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `削除した記録に紐づかない他の記録は残る`() = runBlocking {
        val recordId1 = insertRecordWithItems()
        val recordId2 = insertRecordWithItems()
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.deleteRecord(recordId1)

        assertNull(db.recordDao().getById(recordId1))
        assertNotNull(db.recordDao().getById(recordId2))
        assertEquals(2, db.itemDao().getByRecordIdOnce(recordId2).size)
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        var shouldThrowOnDelete: Boolean = false
        val deletedRemoteIds = mutableListOf<String>()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            // 本テストでは未使用
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            // 本テストでは未使用
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> =
            emptyList()

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> = emptyList()

        override suspend fun deleteRecord(uid: String, remoteId: String) {
            if (shouldThrowOnDelete) throw RuntimeException("Firestore unavailable")
            deletedRemoteIds.add(remoteId)
        }

        override suspend fun deleteAllUserData(uid: String) {
            // 本テストでは未使用
        }
    }
}
