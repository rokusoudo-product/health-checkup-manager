package com.rokusoudo.healthcheckup.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemCategories
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #41: サインアウト時の Room DB クリア（[HealthRepository.clearLocalDataOnSignOut]）を検証する。
 *
 * - サインアウト後、端末の Room DB に健診データ（記録・検査項目）が残っていないこと
 * - 項目マスターは削除されたままにはせず、初期カタログ（DEFAULT_ITEM_MASTERS）へ戻ること
 *   （カスタマイズ済みの基準値・お気に入り状態は消える＝前の利用者の情報を残さない）
 * - 再度同じアカウントでサインインした場合、Firestoreに保存済みのデータで復元されること
 * - 何もデータが無い状態で呼び出しても例外にならないこと（冪等）
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositorySignOutTest {

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

    @Test
    fun `サインアウト後は記録と検査項目がRoomDBに残らない`() = runBlocking {
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
                )
            )
        )
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.clearLocalDataOnSignOut()

        assertNull(db.recordDao().getById(recordId))
        assertTrue(db.itemDao().getByRecordIdOnce(recordId).isEmpty())
    }

    @Test
    fun `サインアウト後は項目マスターが初期カタログへ戻りカスタマイズは残らない`() = runBlocking {
        // 前の利用者が基準値・カテゴリをカスタマイズした項目マスター
        db.masterDao().upsert(
            ItemMaster(
                itemName = "LDLコレステロール",
                unit = "mg/dL",
                referenceMin = 50.0,
                referenceMax = 999.0,
                category = ItemCategories.OTHER,
                isFavorite = true,
                favoritedAt = 12345L
            )
        )
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.clearLocalDataOnSignOut()

        val masters = db.masterDao().getAll().first()
        assertEquals(HealthCheckupDatabase.DEFAULT_ITEM_MASTERS.size, masters.size)
        val ldl = masters.first { it.itemName == "LDLコレステロール" }
        // カスタマイズ（基準値・お気に入り）は消え、初期カタログの値に戻っている
        assertEquals(139.0, ldl.referenceMax)
        assertEquals(false, ldl.isFavorite)
        assertNull(ldl.favoritedAt)
    }

    @Test
    fun `サインアウト後に同じアカウントで再ログインするとFirestoreからデータが復元される`() = runBlocking {
        db.recordDao().insert(ExaminationRecord(date = "2026-08-01", facility = "旧データ", createdAt = 1000L))
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })
        repository.clearLocalDataOnSignOut()

        // Firestoreには記録とカスタマイズ済み項目マスターが保存されている想定
        cloudSync.recordsToReturn = listOf(
            ExaminationRecord(id = 1L, date = "2026-08-01", facility = "六創堂クリニック", createdAt = 2000L) to
                listOf(
                    ExaminationItem(
                        recordId = 1L,
                        itemName = "LDLコレステロール",
                        value = "150",
                        unit = "mg/dL",
                        referenceMin = null,
                        referenceMax = 139.0,
                        isAbnormal = true
                    )
                )
        )
        cloudSync.mastersToReturn = listOf(
            ItemMaster("LDLコレステロール", "mg/dL", null, 139.0, isFavorite = true, favoritedAt = 5000L)
        )

        repository.restoreFromFirestore("test-uid")

        val restoredRecord = db.recordDao().getById(1L)
        assertTrue(restoredRecord != null)
        assertEquals("六創堂クリニック", restoredRecord!!.facility)
        val restoredMaster = db.masterDao().getByName("LDLコレステロール")
        assertTrue(restoredMaster != null)
        assertEquals(true, restoredMaster!!.isFavorite)
    }

    @Test
    fun `データが無い状態でサインアウト処理を呼んでも例外にならない`() = runBlocking {
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.clearLocalDataOnSignOut()

        val masters = db.masterDao().getAll().first()
        assertEquals(HealthCheckupDatabase.DEFAULT_ITEM_MASTERS.size, masters.size)
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        var recordsToReturn: List<Pair<ExaminationRecord, List<ExaminationItem>>> = emptyList()
        var mastersToReturn: List<ItemMaster> = emptyList()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            // 本テストでは未使用
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            // 本テストでは未使用
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
            return recordsToReturn
        }

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> {
            return mastersToReturn
        }

        override suspend fun deleteAllUserData(uid: String) {
            // 本テストでは未使用
        }
    }
}
