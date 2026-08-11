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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #26: アプリ起動時の Firestore 再同期（resyncOnStartupIfNeeded）を検証する。
 *
 * - ログイン済み（uidが取れる）場合のみ再同期が実行されること
 * - 未ログインの場合は何も起こらないこと
 * - 同一インスタンスへの複数回呼び出しでも、実際の同期は1回しか行われないこと（多重実行防止）
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositoryStartupResyncTest {

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
    fun `ログイン済みならアプリ起動時にFirestoreから再同期される`() = runBlocking {
        cloudSync.recordsToReturn = listOf(
            ExaminationRecord(id = 1L, date = "2026-08-01", facility = "Web入力", createdAt = 1000L) to
                listOf(
                    ExaminationItem(
                        recordId = 1L,
                        itemName = "LDL",
                        value = "150",
                        unit = "mg/dL",
                        referenceMin = null,
                        referenceMax = 139.0,
                        isAbnormal = true
                    )
                )
        )
        cloudSync.mastersToReturn = listOf(ItemMaster("LDL", "mg/dL", null, 139.0))
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.resyncOnStartupIfNeeded()

        assertEquals(1, cloudSync.fetchRecordsCallCount)
        assertEquals(1, cloudSync.fetchItemMastersCallCount)
        val records = db.recordDao().getById(1L)
        assertTrue(records != null)
        val items = db.itemDao().getByRecordIdOnce(1L)
        assertEquals(1, items.size)
        assertEquals("LDL", items[0].itemName)
    }

    @Test
    fun `未ログインならFirestoreとの再同期は実行されない`() = runBlocking {
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { null })

        repository.resyncOnStartupIfNeeded()

        assertEquals(0, cloudSync.fetchRecordsCallCount)
        assertEquals(0, cloudSync.fetchItemMastersCallCount)
    }

    @Test
    fun `同一インスタンスに複数回呼び出しても再同期は1回しか実行されない`() = runBlocking {
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        repository.resyncOnStartupIfNeeded()
        repository.resyncOnStartupIfNeeded()
        repository.resyncOnStartupIfNeeded()

        assertEquals(1, cloudSync.fetchRecordsCallCount)
        assertEquals(1, cloudSync.fetchItemMastersCallCount)
    }

    @Test
    fun `Firestore例外時はクラッシュせずローカルデータが保持される`() = runBlocking {
        val localRecordId = db.recordDao().insert(
            ExaminationRecord(date = "2026-07-01", facility = "既存ローカル", createdAt = 500L)
        )
        cloudSync.shouldThrow = true
        val repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })

        // 例外が外に伝播しないこと
        repository.resyncOnStartupIfNeeded()

        val localRecord = db.recordDao().getById(localRecordId)
        assertTrue(localRecord != null)
        assertEquals("既存ローカル", localRecord!!.facility)
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        var recordsToReturn: List<Pair<ExaminationRecord, List<ExaminationItem>>> = emptyList()
        var mastersToReturn: List<ItemMaster> = emptyList()
        var shouldThrow: Boolean = false
        var fetchRecordsCallCount: Int = 0
        var fetchItemMastersCallCount: Int = 0

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            // 本テストでは未使用
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            // 本テストでは未使用
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
            fetchRecordsCallCount++
            if (shouldThrow) throw RuntimeException("Firestore unavailable")
            return recordsToReturn
        }

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> {
            fetchItemMastersCallCount++
            if (shouldThrow) throw RuntimeException("Firestore unavailable")
            return mastersToReturn
        }
    }
}
