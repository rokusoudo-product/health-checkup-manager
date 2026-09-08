package com.rokusoudo.healthcheckup.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.first
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
 * Issue #46: Firestore で削除した記録・項目マスタが Android の Room に残り続け、
 * 基準値変更時に Firestore へ復活する問題への対応（方式A: 差分ミラー削除 + push済みフラグ）を検証する。
 *
 * - Web で削除された記録・項目マスターが、次回の restoreFromFirestore で Room からも削除されること
 * - 項目マスターは削除するのみで、初期カタログ（DEFAULT_ITEM_MASTERS）へは戻さないこと
 * - fetch が失敗した場合は Room から一切削除しないこと
 * - push未確認（オフライン保存直後等）の記録・マスターは削除対象に含まれないこと
 * - upsertMaster() の再計算経路で、Web削除済みの記録が Firestore に再pushされないこと
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositoryMirrorDeleteTest {

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

    @Test
    fun `Webで削除されpush済みだった記録は再同期でRoomから削除される`() = runBlocking {
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
        db.recordDao().markPushed(recordId)

        // Webで削除された = Firestoreのfetch結果にこの記録が含まれない
        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = emptyList()

        repository.restoreFromFirestore("test-uid")

        assertNull(db.recordDao().getById(recordId))
        assertTrue(db.itemDao().getByRecordIdOnce(recordId).isEmpty())
    }

    @Test
    fun `Webで削除されpush済みだった項目マスタは再同期でRoomから削除され初期カタログには戻らない`() = runBlocking {
        db.masterDao().upsert(
            ItemMaster(itemName = "独自項目", unit = "pt", referenceMin = null, referenceMax = 10.0)
        )
        db.masterDao().markPushed("独自項目")

        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = emptyList()

        repository.restoreFromFirestore("test-uid")

        assertNull(db.masterDao().getByName("独自項目"))
        // 初期カタログ（DEFAULT_ITEM_MASTERS）への復帰は行わない
        val allMasters = db.masterDao().getAll().first()
        assertTrue(allMasters.isEmpty())
    }

    @Test
    fun `既定マスタをWebで削除した場合もAndroid側で復活しない`() = runBlocking {
        val defaultMaster = HealthCheckupDatabase.DEFAULT_ITEM_MASTERS.first { it.itemName == "BMI" }
        db.masterDao().upsert(defaultMaster)
        db.masterDao().markPushed("BMI")

        // Webで "BMI" マスタが削除された
        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = emptyList()

        repository.restoreFromFirestore("test-uid")

        assertNull(
            "既定マスタであっても、削除された場合はDEFAULT_ITEM_MASTERSへ復帰させてはならない",
            db.masterDao().getByName("BMI")
        )
    }

    @Test
    fun `Firestore fetchが例外で失敗した場合はpush済みの記録もRoomから削除されない`() = runBlocking {
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = "2026-08-01", facility = "六創堂クリニック", createdAt = 1000L)
        )
        db.recordDao().markPushed(recordId)
        db.masterDao().upsert(ItemMaster(itemName = "独自項目", unit = "pt", referenceMin = null, referenceMax = 10.0))
        db.masterDao().markPushed("独自項目")

        cloudSync.shouldThrow = true

        repository.restoreFromFirestore("test-uid")

        assertNotNull("fetch失敗時はRoomから記録を削除してはならない", db.recordDao().getById(recordId))
        assertNotNull("fetch失敗時はRoomから項目マスタを削除してはならない", db.masterDao().getByName("独自項目"))
    }

    @Test
    fun `push未確認のローカル記録は再同期で削除されない`() = runBlocking {
        // saveRecordのローカル保存直後を想定: pushedToFirestoreはデフォルトfalse（markPushedを呼んでいない）
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = "2026-08-01", facility = "オフライン保存分", createdAt = 1000L)
        )
        val record = db.recordDao().getById(recordId)!!
        assertFalse("前提: push未確認の記録はpushedToFirestore=falseであること", record.pushedToFirestore)

        // Firestore側にはまだこの記録が存在しない（オフライン保存が反映されていない）
        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = emptyList()

        repository.restoreFromFirestore("test-uid")

        assertNotNull(
            "push未確認のローカル記録は、Firestoreのfetch結果に含まれないだけでは削除してはならない",
            db.recordDao().getById(recordId)
        )
    }

    @Test
    fun `push未確認のローカル項目マスタは再同期で削除されない`() = runBlocking {
        db.masterDao().upsert(ItemMaster(itemName = "オフライン項目", unit = "pt", referenceMin = null, referenceMax = 5.0))

        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = emptyList()

        repository.restoreFromFirestore("test-uid")

        assertNotNull(db.masterDao().getByName("オフライン項目"))
    }

    @Test
    fun `基準値変更時にWebで削除済みの記録がFirestoreに再pushされない`() = runBlocking {
        // Web削除前: LDLの記録がRoomにもFirestoreにも存在していた（push済み）
        val recordId = db.recordDao().insert(
            ExaminationRecord(date = "2025-06-01", facility = "テスト病院", createdAt = 1000L)
        )
        db.itemDao().insertAll(
            listOf(
                ExaminationItem(
                    recordId = recordId,
                    itemName = "LDL",
                    value = "150",
                    unit = "mg/dL",
                    referenceMin = null,
                    referenceMax = 160.0,
                    isAbnormal = false
                )
            )
        )
        db.recordDao().markPushed(recordId)
        db.masterDao().upsert(ItemMaster("LDL", "mg/dL", null, 160.0))
        db.masterDao().markPushed("LDL")

        // Webでこの記録が削除された → 次のfetch結果に含まれない
        cloudSync.recordsToReturn = emptyList()
        cloudSync.mastersToReturn = listOf(ItemMaster("LDL", "mg/dL", null, 160.0))
        repository.restoreFromFirestore("test-uid")

        // ミラー削除により、この記録は既にRoomから消えている前提を確認
        assertNull(db.recordDao().getById(recordId))

        // Androidで同じ項目名の基準値を変更する（再計算経路）
        repository.upsertMaster(ItemMaster("LDL", "mg/dL", null, 139.0))

        // 削除済みの記録がFirestoreへ再pushされていないこと
        assertTrue(
            "Web側で削除済みの記録がupsertMasterの再計算経路でFirestoreに復活pushされてはならない",
            cloudSync.savedRecords.isEmpty()
        )
        // マスター自体の同期は引き続き行われる
        assertEquals(1, cloudSync.savedMasters.size)
    }

    private class FakeHealthCloudSync : HealthCloudSync {
        var recordsToReturn: List<Pair<ExaminationRecord, List<ExaminationItem>>> = emptyList()
        var mastersToReturn: List<ItemMaster> = emptyList()
        var shouldThrow: Boolean = false
        val savedRecords = mutableListOf<Pair<ExaminationRecord, List<ExaminationItem>>>()
        val savedMasters = mutableListOf<ItemMaster>()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            savedRecords.add(record to items)
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            savedMasters.add(master)
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
            if (shouldThrow) throw RuntimeException("Firestore unavailable")
            return recordsToReturn
        }

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> {
            if (shouldThrow) throw RuntimeException("Firestore unavailable")
            return mastersToReturn
        }

        override suspend fun deleteAllUserData(uid: String) {
            // Issue #46 のミラー削除テストの対象外（Issue #34 のアカウント削除専用メソッド）
        }
    }
}
