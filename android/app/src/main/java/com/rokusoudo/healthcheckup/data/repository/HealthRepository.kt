package com.rokusoudo.healthcheckup.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.rokusoudo.healthcheckup.OcrItem
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.dao.ItemTrend
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.Flow

/**
 * 健康診断データの Repository。
 * Room DB への読み書きと Cloud Firestore との同期を一元管理する。
 * 薬事法対応: isAbnormal は表示のみに使用し、医療診断を目的としない。
 */
class HealthRepository(
    private val db: HealthCheckupDatabase,
    private val firestoreRepository: FirestoreRepository
) {

    /**
     * OCR結果・手動入力を診断記録として保存する。
     * Room への保存後、Firestoreへ非同期で同期する。
     * Firestore同期失敗はローカル保存に影響しない。
     * @return 生成された ExaminationRecord の id
     */
    suspend fun saveRecord(date: String, facility: String, items: List<OcrItem>): Long {
        val record = ExaminationRecord(
            date = date,
            facility = facility,
            createdAt = System.currentTimeMillis()
        )
        val recordId = db.recordDao().insert(record)

        val examinationItems = items.map { ocrItem ->
            val master = db.masterDao().getByName(ocrItem.itemName)
            val numericValue = ocrItem.value.toDoubleOrNull()
            val isAbnormal = if (numericValue != null && master != null) {
                val belowMin = master.referenceMin?.let { numericValue < it } ?: false
                val aboveMax = master.referenceMax?.let { numericValue > it } ?: false
                belowMin || aboveMax
            } else {
                false
            }
            ExaminationItem(
                recordId = recordId,
                itemName = ocrItem.itemName,
                value = ocrItem.value,
                unit = ocrItem.unit,
                referenceMin = master?.referenceMin,
                referenceMax = master?.referenceMax,
                isAbnormal = isAbnormal
            )
        }
        db.itemDao().insertAll(examinationItems)

        // Firestore同期（失敗してもローカル保存は維持）
        syncRecordToFirestore(record.copy(id = recordId), examinationItems)

        return recordId
    }

    private suspend fun syncRecordToFirestore(record: ExaminationRecord, items: List<ExaminationItem>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            firestoreRepository.saveRecord(uid, record, items)
        } catch (_: Exception) {
            // オフライン時など同期失敗は無視
        }
    }

    fun getAllRecords(): Flow<List<ExaminationRecord>> = db.recordDao().getAll()

    /** 記録ごとの基準値外項目数（診断記録一覧の状態バッジ用）。 */
    fun getAbnormalCountsByRecord(): Flow<List<com.rokusoudo.healthcheckup.data.db.dao.RecordAbnormalCount>> =
        db.itemDao().getAbnormalCountsByRecord()

    fun getItemsForRecord(recordId: Long): Flow<List<ExaminationItem>> =
        db.itemDao().getByRecordId(recordId)

    fun getAllMasters(): Flow<List<ItemMaster>> = db.masterDao().getAll()

    // TODO: 項目マスターの基準値変更時、既存の ExaminationItem.isAbnormal は再計算されない。
    // P1スコープ外のため対応保留（次回スプリントで対応予定）。
    suspend fun upsertMaster(master: ItemMaster) {
        db.masterDao().upsert(master)
        // 項目マスターもFirestoreへ同期
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            firestoreRepository.saveItemMaster(uid, master)
        } catch (_: Exception) {}
    }

    /**
     * 指定した recordId の検査項目を一度だけ取得する（通知判定用）。
     */
    suspend fun getItemsForRecordSnapshot(recordId: Long): List<ExaminationItem> =
        db.itemDao().getByRecordIdOnce(recordId)

    /**
     * 指定した項目名の経年データを日付昇順で取得する（グラフ表示用）。
     */
    suspend fun getItemTrend(itemName: String): List<ItemTrend> =
        db.itemDao().getItemTrendByName(itemName)

    /**
     * 基準値外の全検査項目を新しい順で取得する（Flow）。
     */
    fun getAllAbnormalItems(): Flow<List<ExaminationItem>> =
        db.itemDao().getAllAbnormalItems()

    /**
     * T-403: Firestoreから全データを取得してRoomへ復元する。
     * ログイン成功後に呼び出す（他端末のデータをローカルに同期）。
     */
    suspend fun restoreFromFirestore(uid: String) {
        try {
            // 診断記録を復元
            val records = firestoreRepository.fetchRecords(uid)
            for ((record, items) in records) {
                db.recordDao().upsert(record)
                db.itemDao().deleteByRecordId(record.id)
                db.itemDao().insertAll(items)
            }
            // 項目マスターを復元
            val masters = firestoreRepository.fetchItemMasters(uid)
            for (master in masters) {
                db.masterDao().upsert(master)
            }
        } catch (_: Exception) {
            // ネットワーク不可時は無視（既存のローカルデータをそのまま使用）
        }
    }
}
