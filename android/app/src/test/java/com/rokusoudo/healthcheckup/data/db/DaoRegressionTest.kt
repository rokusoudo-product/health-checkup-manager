package com.rokusoudo.healthcheckup.data.db

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room DAO のリグレッションテスト（画面遷移刷新001・Phase1）。
 * in-memory DB のため初期シード（onCreate コールバック）は投入されない前提で書く。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DaoRegressionTest {

    private lateinit var db: HealthCheckupDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun insertRecordWithItems(
        date: String,
        items: List<Triple<String, String, Boolean>> // itemName, value, isAbnormal
    ): Long {
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = date, facility = "テスト病院", createdAt = 1000L)
        )
        db.itemDao().insertAll(items.map { (name, value, abnormal) ->
            ExaminationItem(
                recordId = recordId, itemName = name, value = value, unit = "",
                referenceMin = null, referenceMax = null, isAbnormal = abnormal
            )
        })
        return recordId
    }

    @Test
    fun `記録の保存と取得ができる`() = runBlocking {
        val id = insertRecordWithItems("2025-06-01", listOf(Triple("体重", "68.5", false)))
        val record = db.recordDao().getById(id)
        assertNotNull(record)
        assertEquals("2025-06-01", record!!.date)
        assertEquals(1, db.itemDao().getByRecordIdOnce(id).size)
    }

    @Test
    fun `項目別トレンドは記録日の昇順で返る`() = runBlocking {
        insertRecordWithItems("2025-06-01", listOf(Triple("BMI", "22.0", false)))
        insertRecordWithItems("2024-06-01", listOf(Triple("BMI", "23.5", false)))
        insertRecordWithItems("2023-06-01", listOf(Triple("BMI", "24.0", false)))

        val trend = db.itemDao().getItemTrendByName("BMI")
        assertEquals(listOf("2023-06-01", "2024-06-01", "2025-06-01"), trend.map { it.recordDate })
        assertEquals(listOf("24.0", "23.5", "22.0"), trend.map { it.value })
    }

    @Test
    fun `基準値外の項目のみが基準値外一覧に返る`() = runBlocking {
        insertRecordWithItems(
            "2025-06-01",
            listOf(Triple("LDL", "160", true), Triple("HDL", "55", false))
        )
        val abnormal = db.itemDao().getAllAbnormalItems().first()
        assertEquals(listOf("LDL"), abnormal.map { it.itemName })
    }

    @Test
    fun `記録ごとの基準値外件数が集計できる`() = runBlocking {
        val id1 = insertRecordWithItems(
            "2025-06-01",
            listOf(Triple("LDL", "160", true), Triple("AST", "40", true))
        )
        insertRecordWithItems("2024-06-01", listOf(Triple("HDL", "55", false)))

        val counts = db.itemDao().getAbnormalCountsByRecord().first()
        assertEquals(1, counts.size)
        assertEquals(id1, counts[0].recordId)
        assertEquals(2, counts[0].count)
    }

    @Test
    fun `項目名指定で検査項目を全件取得できる`() = runBlocking {
        insertRecordWithItems("2025-06-01", listOf(Triple("LDL", "150", false), Triple("HDL", "55", false)))
        insertRecordWithItems("2024-06-01", listOf(Triple("LDL", "160", true)))

        val items = db.itemDao().getByItemName("LDL")
        assertEquals(2, items.size)
        assertEquals(setOf("150", "160"), items.map { it.value }.toSet())
    }

    @Test
    fun `updateAllで検査項目の基準値と判定を一括更新できる`() = runBlocking {
        insertRecordWithItems("2025-06-01", listOf(Triple("LDL", "150", false)))
        val items = db.itemDao().getByItemName("LDL")

        db.itemDao().updateAll(
            items.map { it.copy(referenceMin = null, referenceMax = 139.0, isAbnormal = true) }
        )

        val updated = db.itemDao().getByItemName("LDL")
        assertEquals(1, updated.size)
        assertEquals(139.0, updated[0].referenceMax!!, 0.0)
        assertEquals(true, updated[0].isAbnormal)
    }

    @Test
    fun `項目マスターのupsertと取得ができる`() = runBlocking {
        db.masterDao().upsert(ItemMaster(itemName = "BMI", unit = "kg/m2", referenceMin = 18.5, referenceMax = 25.0))
        db.masterDao().upsert(ItemMaster(itemName = "BMI", unit = "kg/m2", referenceMin = 18.5, referenceMax = 24.9))

        val master = db.masterDao().getByName("BMI")
        assertNotNull(master)
        assertEquals(24.9, master!!.referenceMax!!, 0.0)
        assertNull(db.masterDao().getByName("存在しない項目"))
        assertEquals(1, db.masterDao().getAll().first().size)
    }
}
