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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #49: Firestoreの記録ドキュメントIDを端末ローカルの連番からremoteId（UUID）へ切り替えたことを検証する。
 *
 * - restoreFromFirestore() がremoteIdをキーに突き合わせ、同じ記録が二重にRoomへ入らないこと
 * - 2台の端末がそれぞれ端末ローカル採番id=2の記録を作っても、Firestore上（＝共有のHealthCloudSync）では
 *   別ドキュメントとして扱われ、互いを上書きしないこと（本Issueが修正する不具合そのものの回帰テスト）
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HealthRepositoryRemoteIdSyncTest {

    private lateinit var db: HealthCheckupDatabase
    private lateinit var cloudSync: FakeSharedFirestore
    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        cloudSync = FakeSharedFirestore()
        repository = HealthRepository(db, cloudSync, currentUidProvider = { "shared-uid" })
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `restoreFromFirestoreはremoteIdをキーに突き合わせ同じ記録を二重にRoomへ入れない`() = runBlocking {
        val remoteId = "web-uuid-1"
        cloudSync.recordsToReturn = listOf(
            ExaminationRecord(id = 0, date = "2026-08-01", facility = "Web入力", createdAt = 1000L, remoteId = remoteId) to
                listOf(
                    ExaminationItem(
                        recordId = 0,
                        itemName = "LDL",
                        value = "150",
                        unit = "mg/dL",
                        referenceMin = null,
                        referenceMax = 139.0,
                        isAbnormal = true
                    )
                )
        )

        // 同じfetch結果で2回連続復元しても重複行が作られないこと（アプリ起動のたびに再同期されるため）
        repository.restoreFromFirestore("shared-uid")
        repository.restoreFromFirestore("shared-uid")

        val all = db.recordDao().getAll().first()
        assertEquals(1, all.size)
        assertEquals(remoteId, all[0].remoteId)
        assertEquals(1, db.itemDao().getByRecordIdOnce(all[0].id).size)
    }

    @Test
    fun `remoteIdが一致する既存ローカル記録は端末ローカルidを保ったまま内容が更新される`() = runBlocking {
        val remoteId = "web-uuid-2"
        val localId = db.recordDao().insert(
            ExaminationRecord(date = "2026-07-01", facility = "旧内容", createdAt = 500L, remoteId = remoteId)
        )

        cloudSync.recordsToReturn = listOf(
            ExaminationRecord(id = 0, date = "2026-08-01", facility = "新内容", createdAt = 2000L, remoteId = remoteId) to
                emptyList()
        )
        repository.restoreFromFirestore("shared-uid")

        val all = db.recordDao().getAll().first()
        assertEquals("突き合わせに失敗して新規行が作られてはならない", 1, all.size)
        assertEquals(localId, all[0].id)
        assertEquals("新内容", all[0].facility)
    }

    /**
     * Issue #49 本文の再現シナリオそのもの:
     * 「同じGoogleアカウントで2台のAndroid端末を使い、両方が端末ローカルで id=2 を採番しても、
     *  Firestore上（本テストでは2つの HealthRepository インスタンスが共有する FakeSharedFirestore）
     *  では別ドキュメントとして保存され、どちらの記録も消えない」ことを確認する。
     */
    @Test
    fun `2台の端末がそれぞれローカルid2の記録を作ってもFirestore上は別ドキュメントになる`() = runBlocking {
        // 端末A: 別々のRoom DBインスタンス（別端末を表す）
        val dbA = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        val repoA = HealthRepository(dbA, cloudSync, currentUidProvider = { "shared-uid" })

        // 端末B
        val dbB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HealthCheckupDatabase::class.java
        ).allowMainThreadQueries().build()
        val repoB = HealthRepository(dbB, cloudSync, currentUidProvider = { "shared-uid" })

        try {
            // 各端末で1件目を保存（端末ローカルid=1になる想定。前提の確認のみで本題ではない）
            repoA.saveRecord("2026-08-01", "端末A・1件目", emptyList())
            repoB.saveRecord("2026-08-02", "端末B・1件目", emptyList())

            // 各端末で2件目を保存 → 両端末とも端末ローカルAUTOINCREMENTでid=2になる（衝突の前提）
            val localIdA2 = repoA.saveRecord("2026-08-03", "端末A・2件目", emptyList())
            val localIdB2 = repoB.saveRecord("2026-08-04", "端末B・2件目", emptyList())
            assertEquals("前提: 端末Aの2件目は端末ローカルid=2", 2L, localIdA2)
            assertEquals("前提: 端末Bの2件目も端末ローカルid=2（衝突の前提）", 2L, localIdB2)

            // 修正前の実装（ドキュメントID = Roomのローカルid）なら、この時点で
            // Firestore上は users/shared-uid/records/2 が後勝ちで1件しか残らず、データが消える。
            // 修正後はremoteId（UUID）がドキュメントIDのため、4件とも別ドキュメントとして残るはず。
            val allDocs = cloudSync.fetchRecords("shared-uid")
            assertEquals(
                "端末ローカルidが衝突していても、Firestore上のドキュメントは4件とも残らなければならない",
                4,
                allDocs.size
            )

            val remoteIdsUsed = allDocs.map { it.first.remoteId }
            assertEquals("ドキュメントIDに重複があってはならない（=上書き衝突が起きている）", 4, remoteIdsUsed.toSet().size)

            // 特に衝突の当事者だった「各端末の2件目」の記録が両方とも生き残っていること
            val facilities = allDocs.map { it.first.facility }.toSet()
            assertTrue(facilities.contains("端末A・2件目"))
            assertTrue(facilities.contains("端末B・2件目"))

            // 各端末が保存に使ったドキュメントIDそのものも異なること（衝突していた旧仕様との対比）
            val recordA2 = dbA.recordDao().getById(localIdA2)!!
            val recordB2 = dbB.recordDao().getById(localIdB2)!!
            assertNotEquals(
                "旧実装ではここが両方とも\"2\"になり衝突していた",
                recordA2.remoteId,
                recordB2.remoteId
            )
        } finally {
            dbA.close()
            dbB.close()
        }
    }

    /**
     * HealthCloudSync のフェイク実装。実際のFirestoreと同様、ドキュメントID（record.remoteId）を
     * キーとしたMapへの `set()` 相当（完全上書き・mergeなし）で保存する。
     * 複数の HealthRepository インスタンス（＝複数端末を表す）から同じインスタンスを共有させることで、
     * 「Firestore上でドキュメントIDが衝突するかどうか」を検証できる。
     */
    private class FakeSharedFirestore : HealthCloudSync {
        var recordsToReturn: List<Pair<ExaminationRecord, List<ExaminationItem>>> = emptyList()

        // uid -> (remoteId -> 記録)。実際のFirestoreの `records` サブコレクションを模す。
        private val store = mutableMapOf<String, MutableMap<String, Pair<ExaminationRecord, List<ExaminationItem>>>>()

        override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
            val bucket = store.getOrPut(uid) { mutableMapOf() }
            // Firestoreの set()（mergeなし）を模す: 同じremoteId（ドキュメントID）への書き込みは完全上書きする
            bucket[record.remoteId] = record to items
        }

        override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
            // 本テストでは未使用
        }

        override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
            if (recordsToReturn.isNotEmpty()) return recordsToReturn
            return store[uid]?.values?.toList() ?: emptyList()
        }

        override suspend fun fetchItemMasters(uid: String): List<ItemMaster> = emptyList()

        override suspend fun deleteRecord(uid: String, remoteId: String) {
            store[uid]?.remove(remoteId)
        }

        override suspend fun deleteAllUserData(uid: String) {
            store.remove(uid)
        }
    }
}
