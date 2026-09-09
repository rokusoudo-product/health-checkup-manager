package com.rokusoudo.healthcheckup.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemCategories
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Cloud Firestore との読み書きを担当する Repository。
 * データパス: users/{uid}/records/{remoteId}
 *             users/{uid}/itemMasters/{itemName}
 *
 * Issue #49: records のドキュメント ID は端末ローカルの Room id ではなく
 * [ExaminationRecord.remoteId]（グローバルに一意な UUID）を使う。
 * 既存の数値IDドキュメント（移行前に作成された記録）とは共存し、一括移行はしない
 * （詳細は [com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase.MIGRATION_3_4] 参照）。
 *
 * 薬事法対応: 保存・取得はデータの表示目的のみ。医療診断に使用しない。
 */
class FirestoreRepository : HealthCloudSync {

    private val db: FirebaseFirestore = Firebase.firestore

    private fun recordsRef(uid: String) =
        db.collection("users").document(uid).collection("records")

    private fun mastersRef(uid: String) =
        db.collection("users").document(uid).collection("itemMasters")

    /**
     * 診断記録をFirestoreに保存する。
     * ドキュメントID = [ExaminationRecord.remoteId]（UUID。Issue #49でRoomのローカルidから変更）。
     * これによりRoomのidが端末間で衝突しても、Firestore上のドキュメントは衝突しない。
     */
    override suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>) {
        val data = mapOf(
            "date" to record.date,
            "facility" to record.facility,
            "createdAt" to record.createdAt,
            "items" to items.map { item ->
                mapOf(
                    "itemName" to item.itemName,
                    "value" to item.value,
                    "unit" to item.unit,
                    "referenceMin" to item.referenceMin,
                    "referenceMax" to item.referenceMax,
                    "isAbnormal" to item.isAbnormal
                )
            }
        )
        recordsRef(uid).document(record.remoteId).set(data).await()
    }

    /**
     * 項目マスターをFirestoreに保存する。
     * ドキュメントID = itemName。
     */
    override suspend fun saveItemMaster(uid: String, master: ItemMaster) {
        mastersRef(uid).document(master.itemName).set(
            mapOf(
                "unit" to master.unit,
                "referenceMin" to master.referenceMin,
                "referenceMax" to master.referenceMax,
                "category" to master.category,
                "isFavorite" to master.isFavorite,
                "favoritedAt" to master.favoritedAt
            )
        ).await()
    }

    /**
     * Firestoreから全診断記録を取得する（他端末データ復元用）。
     *
     * Issue #49: ドキュメントID（[ExaminationRecord.remoteId]）は数値ID（移行前の既存記録）と
     * UUID（移行後の新規記録）の2形式が混在し得るため、[doc.id] を Long に変換できない
     * ドキュメントを黙って捨てていた従来の実装（`doc.id.toLongOrNull() ?: return@mapNotNull null`）
     * を廃止した。この判定を残したままUUIDを導入すると、新形式のドキュメントが
     * fetchRecords() の結果から全て消え、restoreFromFirestore() の差分ミラー削除により
     * 他端末で保存した新形式の記録がRoomから誤って削除されてしまう。
     *
     * 返却する [ExaminationRecord.id] は端末ローカルの主キーであり、Firestore側には存在しない情報
     * のため 0（未確定）を設定する。実際のローカルidは呼び出し元（HealthRepository.restoreFromFirestore）が
     * [ExaminationRecord.remoteId] をキーに端末ローカルの既存行と突き合わせたうえで確定する。
     * 同様に [ExaminationItem.recordId] もこの時点では確定しないため 0 を設定する。
     */
    override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
        val snapshot = recordsRef(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            val date = doc.getString("date") ?: return@mapNotNull null
            val facility = doc.getString("facility") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val record = ExaminationRecord(
                id = 0,
                date = date,
                facility = facility,
                createdAt = createdAt,
                remoteId = doc.id
            )

            @Suppress("UNCHECKED_CAST")
            val itemsData = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
            val items = itemsData.map { map ->
                ExaminationItem(
                    id = 0,
                    recordId = 0,
                    itemName = map["itemName"] as? String ?: "",
                    value = map["value"] as? String ?: "",
                    unit = map["unit"] as? String ?: "",
                    referenceMin = (map["referenceMin"] as? Number)?.toDouble(),
                    referenceMax = (map["referenceMax"] as? Number)?.toDouble(),
                    isAbnormal = map["isAbnormal"] as? Boolean ?: false
                )
            }
            record to items
        }
    }

    /**
     * Issue #47: 診断記録を1件、Firestoreから削除する。
     * ドキュメントID = [ExaminationRecord.remoteId]（Issue #49でRoomのローカルidから変更）。
     * 失敗時は例外を呼び出し元へ伝播する。
     *
     * Firestore SDK はデフォルトでオフライン永続化が有効なため、オフライン時の
     * delete().await() はネットワーク復帰まで完了しない（すぐには例外を投げない）。
     * HealthRepository.deleteRecord は「Firestore成功が確認できるまでRoomを消さない」
     * 設計のため、これをタイムアウトなしで待つと削除操作がUI上で無期限にハングし、
     * ユーザーへエラー提示すらできなくなる（Issue #47の「握りつぶさない」要求に反する）。
     * そのためタイムアウトを設け、TimeoutCancellationException を通常の失敗として
     * HealthRepository側のcatchに流す。タイムアウト後もSDKに削除自体はキューされ得るが、
     * その場合はIssue #46（方式A）の次回restoreFromFirestoreの差分ミラー削除がRoom側を追従させる。
     */
    override suspend fun deleteRecord(uid: String, remoteId: String) {
        withTimeout(DELETE_TIMEOUT_MS) {
            recordsRef(uid).document(remoteId).delete().await()
        }
    }

    /**
     * Firestoreから全項目マスターを取得する（他端末データ復元用）。
     */
    override suspend fun fetchItemMasters(uid: String): List<ItemMaster> {
        val snapshot = mastersRef(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            ItemMaster(
                itemName = doc.id,
                unit = doc.getString("unit") ?: "",
                referenceMin = (doc.get("referenceMin") as? Number)?.toDouble(),
                referenceMax = (doc.get("referenceMax") as? Number)?.toDouble(),
                // 刷新001以前のドキュメントにはフィールドが無いためデフォルトで補完（後方互換）
                category = doc.getString("category") ?: ItemCategories.OTHER,
                isFavorite = doc.getBoolean("isFavorite") ?: false,
                favoritedAt = doc.getLong("favoritedAt")
            )
        }
    }

    /**
     * Issue #34: アカウント削除機能用。
     * users/{uid} 配下の records・itemMasters コレクションの全ドキュメントを削除したのち、
     * users/{uid} 自体のドキュメントも削除する（フィールドを持たないため通常はno-op）。
     */
    override suspend fun deleteAllUserData(uid: String) {
        deleteAllDocuments(recordsRef(uid))
        deleteAllDocuments(mastersRef(uid))
        db.collection("users").document(uid).delete().await()
    }

    /**
     * コレクション内の全ドキュメントを削除する。
     * WriteBatch は1回あたり最大500件までしか操作できないため、500件ごとに分割してコミットする。
     */
    private suspend fun deleteAllDocuments(collection: CollectionReference) {
        val snapshot = collection.get().await()
        if (snapshot.isEmpty) return
        snapshot.documents.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        }
    }

    private companion object {
        /** Issue #47: 記録削除のFirestore待ち上限（オフライン時に無期限ハングしないため）。 */
        const val DELETE_TIMEOUT_MS = 10_000L
        const val FIRESTORE_BATCH_LIMIT = 500
    }
}
