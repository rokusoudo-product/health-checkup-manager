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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #8: 項目マスターの基準値変更時、既存の ExaminationItem の isAbnormal 等が
 * 再計算されること・不要な更新やFirestore再同期が起きないことを検証する。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositoryRecalcTest {

    private lateinit var db: HealthCheckupDatabase
    private lateinit var cloudSync: FakeHealthCloudSync
    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        cloudSync = FakeHealthCloudSync()
        repository = HealthRepository(db, cloudSync, currentUidProvider = { "test-uid" })
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun insertRecordWithItem(
        date: String,
        itemName: String,
        value: String,
        referenceMin: Double?,
        referenceMax: Double?,
        isAbnormal: Boolean
    ): Long {
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = date, facility = "テスト病院", createdAt = 1000L)
        )
        db.itemDao().insertAll(
            listOf(
                ExaminationItem(
                    recordId = recordId,
                    itemName = itemName,
                    value = value,
                    unit = "mg/dL",
                    referenceMin = referenceMin,
                    referenceMax = referenceMax,
                    isAbnormal = isAbnormal
                )
            )
        )
        return recordId
    }

    @Test
    fun `基準値が変更されると既存レコードの基準値外判定が基準値内から基準値外に更新される`() = runBlocking {
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 160.0))
        val recordId = insertRecordWithItem(
            "2025-06-01", "LDL", "150", referenceMin = null, referenceMax = 160.0, isAbnormal = false
        )

        // 基準値を厳しくする（150は新基準では基準値外になる）
        repository.upsertMaster(ItemMaster("LDL", "mg/dL", null, 139.0))

        val items = db.itemDao().getByRecordIdOnce(recordId)
        assertEquals(1, items.size)
        assertTrue(items[0].isAbnormal)
        assertEquals(139.0, items[0].referenceMax!!, 0.0)
    }

    @Test
    fun `基準値が変更されると既存レコードの基準値外判定が基準値外から基準値内に更新される`() = runBlocking {
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 139.0))
        val recordId = insertRecordWithItem(
            "2025-06-01", "LDL", "150", referenceMin = null, referenceMax = 139.0, isAbnormal = true
        )

        // 基準値を緩める（150は新基準では基準値内になる）
        repository.upsertMaster(ItemMaster("LDL", "mg/dL", null, 160.0))

        val items = db.itemDao().getByRecordIdOnce(recordId)
        assertEquals(1, items.size)
        assertFalse(items[0].isAbnormal)
        assertEquals(160.0, items[0].referenceMax!!, 0.0)
    }

    @Test
    fun `お気に入りトグルのみでは既存レコードのisAbnormalは変化しない`() = runBlocking {
        val original = ItemMaster("LDL", "mg/dL", null, 139.0, isFavorite = false)
        db.masterDao().upsert(original)
        val recordId = insertRecordWithItem(
            "2025-06-01", "LDL", "150", referenceMin = null, referenceMax = 139.0, isAbnormal = true
        )

        repository.upsertMaster(original.copy(isFavorite = true, favoritedAt = 12345L))

        val items = db.itemDao().getByRecordIdOnce(recordId)
        assertEquals(1, items.size)
        // 値・基準値外判定とも変化しないこと
        assertTrue(items[0].isAbnormal)
        assertEquals(139.0, items[0].referenceMax!!, 0.0)
        // Firestoreへの記録再同期も発生しないこと（マスター同期のみ）
        assertTrue(cloudSync.savedRecords.isEmpty())
    }

    @Test
    fun `カテゴリ変更のみでは既存レコードのisAbnormalは変化しない`() = runBlocking {
        val original = ItemMaster("LDL", "mg/dL", null, 139.0, category = "脂質")
        db.masterDao().upsert(original)
        val recordId = insertRecordWithItem(
            "2025-06-01", "LDL", "120", referenceMin = null, referenceMax = 139.0, isAbnormal = false
        )

        repository.upsertMaster(original.copy(category = "その他"))

        val items = db.itemDao().getByRecordIdOnce(recordId)
        assertEquals(1, items.size)
        assertFalse(items[0].isAbnormal)
        assertEquals(139.0, items[0].referenceMax!!, 0.0)
        assertTrue(cloudSync.savedRecords.isEmpty())
    }

    @Test
    fun `再計算対象の記録はFirestoreにも再同期される`() = runBlocking {
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 160.0))
        val recordId = insertRecordWithItem(
            "2025-06-01", "LDL", "150", referenceMin = null, referenceMax = 160.0, isAbnormal = false
        )

        repository.upsertMaster(ItemMaster("LDL", "mg/dL", null, 139.0))

        assertEquals(1, cloudSync.savedRecords.size)
        val (syncedRecord, syncedItems) = cloudSync.savedRecords[0]
        assertEquals(recordId, syncedRecord.id)
        assertEquals(1, syncedItems.size)
        assertTrue(syncedItems[0].isAbnormal)
        // マスターの同期も呼ばれていること
        assertEquals(1, cloudSync.savedMasters.size)
    }

    @Test
    fun `evaluateAbnormalは最小値未満と最大値超過を基準値外と判定する`() {
        assertTrue(HealthRepository.evaluateAbnormal("100", 120.0, null))
        assertTrue(HealthRepository.evaluateAbnormal("200", null, 150.0))
        assertFalse(HealthRepository.evaluateAbnormal("130", 120.0, 150.0))
        assertFalse(HealthRepository.evaluateAbnormal(null, 120.0, 150.0))
        assertFalse(HealthRepository.evaluateAbnormal("abc", 120.0, 150.0))
    }

    @Test
    fun `referenceRangeChangedは基準値の変化のみを検知する`() {
        val base = ItemMaster("LDL", "mg/dL", null, 139.0, category = "脂質", isFavorite = false)

        assertFalse(HealthRepository.referenceRangeChanged(base, base.copy(isFavorite = true, favoritedAt = 1L)))
        assertFalse(HealthRepository.referenceRangeChanged(base, base.copy(category = "その他")))
        assertTrue(HealthRepository.referenceRangeChanged(base, base.copy(referenceMax = 150.0)))
        assertTrue(HealthRepository.referenceRangeChanged(base, base.copy(referenceMin = 50.0)))
        assertTrue(HealthRepository.referenceRangeChanged(null, base))
        assertFalse(HealthRepository.referenceRangeChanged(null, base.copy(referenceMin = null, referenceMax = null)))
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        val savedRecords = mutableListOf<Pair<ExaminationRecord, List<ExaminationItem>>>()
        val savedMasters = mutableListOf<ItemMaster>()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            savedRecords.add(record to items)
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            savedMasters.add(master)
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> =
            emptyList()

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> = emptyList()
    }
}
