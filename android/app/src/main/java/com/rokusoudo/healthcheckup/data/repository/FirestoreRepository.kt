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

/**
 * Cloud Firestore との読み書きを担当する Repository。
 * データパス: users/{uid}/records/{roomId}
 *             users/{uid}/itemMasters/{itemName}
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
     * ドキュメントID = Room の recordId（文字列）でクロスプラットフォーム同期を担保。
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
        recordsRef(uid).document(record.id.toString()).set(data).await()
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
     */
    override suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>> {
        val snapshot = recordsRef(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id.toLongOrNull() ?: return@mapNotNull null
            val date = doc.getString("date") ?: return@mapNotNull null
            val facility = doc.getString("facility") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val record = ExaminationRecord(id = id, date = date, facility = facility, createdAt = createdAt)

            @Suppress("UNCHECKED_CAST")
            val itemsData = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
            val items = itemsData.map { map ->
                ExaminationItem(
                    id = 0,
                    recordId = id,
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

    companion object {
        private const val FIRESTORE_BATCH_LIMIT = 500
    }
}
