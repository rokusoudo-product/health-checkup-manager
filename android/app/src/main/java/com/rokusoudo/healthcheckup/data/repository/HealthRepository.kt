package com.rokusoudo.healthcheckup.data.repository

import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.rokusoudo.healthcheckup.OcrItem
import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.dao.ItemTrend
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 健康診断データの Repository。
 * Room DB への読み書きと Cloud Firestore との同期を一元管理する。
 * 薬事法対応: isAbnormal は表示のみに使用し、医療診断を目的としない。
 *
 * @param currentUidProvider ログイン中ユーザーの uid 取得手段。既定は FirebaseAuth だが、
 *   テストではフェイクの uid を返す関数に差し替え可能。
 */
class HealthRepository(
    private val db: HealthCheckupDatabase,
    private val firestoreRepository: HealthCloudSync,
    private val currentUidProvider: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid }
) {

    /** Issue #26: アプリ起動時の再同期が既に実行された（または実行中）かどうか。1起動＝1インスタンス分に限り一度だけ true になる。 */
    private val startupResyncAttempted = AtomicBoolean(false)

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
            ExaminationItem(
                recordId = recordId,
                itemName = ocrItem.itemName,
                value = ocrItem.value,
                unit = ocrItem.unit,
                referenceMin = master?.referenceMin,
                referenceMax = master?.referenceMax,
                isAbnormal = evaluateAbnormal(ocrItem.value, master?.referenceMin, master?.referenceMax)
            )
        }
        db.itemDao().insertAll(examinationItems)

        // Firestore同期（失敗してもローカル保存は維持）
        syncRecordToFirestore(record.copy(id = recordId), examinationItems)

        return recordId
    }

    private suspend fun syncRecordToFirestore(record: ExaminationRecord, items: List<ExaminationItem>) {
        val uid = currentUidProvider() ?: return
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

    /**
     * 項目マスターを更新する。
     * 基準値（referenceMin / referenceMax）が実際に変化した場合のみ、
     * 当該項目名の既存 ExaminationItem 全件を新基準で再計算する
     * （カテゴリ変更・お気に入りトグルでは再計算しない＝不要な全件 UPDATE を避ける）。
     * 再計算対象となった記録は Firestore にも再同期する。
     */
    suspend fun upsertMaster(master: ItemMaster) {
        val previous = db.masterDao().getByName(master.itemName)
        val shouldRecalculate = referenceRangeChanged(previous, master)

        val affectedRecordIds: List<Long> = if (shouldRecalculate) {
            db.withTransaction {
                db.masterDao().upsert(master)
                val existingItems = db.itemDao().getByItemName(master.itemName)
                val recalculated = existingItems.map { item ->
                    item.copy(
                        referenceMin = master.referenceMin,
                        referenceMax = master.referenceMax,
                        isAbnormal = evaluateAbnormal(item.value, master.referenceMin, master.referenceMax)
                    )
                }
                if (recalculated.isNotEmpty()) {
                    db.itemDao().updateAll(recalculated)
                }
                recalculated.map { it.recordId }.distinct()
            }
        } else {
            db.masterDao().upsert(master)
            emptyList()
        }

        // 項目マスターもFirestoreへ同期
        val uid = currentUidProvider() ?: return
        try {
            firestoreRepository.saveItemMaster(uid, master)
        } catch (_: Exception) {
            // オフライン時など同期失敗は無視
        }

        // 再計算対象の記録をFirestoreにも再同期し、Web版との整合を保つ
        for (recordId in affectedRecordIds) {
            val record = db.recordDao().getById(recordId) ?: continue
            val itemsForRecord = db.itemDao().getByRecordIdOnce(recordId)
            try {
                firestoreRepository.saveRecord(uid, record, itemsForRecord)
            } catch (_: Exception) {
                // オフライン時など同期失敗は無視
            }
        }
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
     * Issue #41: サインアウト時に端末の Room DB から健診データを消去する。
     *
     * 共用端末でサインアウトしても前の利用者の健診記録・項目マスターのカスタマイズ
     * （基準値編集・お気に入り等）が閲覧できてしまう問題への対応。
     * アカウント削除（Issue #34, `AccountDeletionManager.deleteAllLocalHealthData`）とは別処理:
     * こちらは削除確認ダイアログを伴わないサインアウトの一部として、既存UXを変えずに実行する。
     *
     * `db.clearAllTables()` は診断記録・検査項目・項目マスターの全テーブルを消去するが、
     * DB作成時のみ実行される `PREPOPULATE_CALLBACK` は再実行されないため、
     * 項目マスターは [HealthCheckupDatabase.DEFAULT_ITEM_MASTERS] を明示的に再投入し、
     * 端末を初期状態のカタログへ戻す（削除ではなく「初期化」）。
     *
     * 再度同じアカウントでサインインすると `LoginViewModel.signIn()` 経由で
     * [restoreFromFirestore] が呼ばれ、Firestore に保存済みの記録・カスタマイズ済み項目マスターが
     * itemName をキーに上書き復元される（Firestoreにない項目は初期値のまま残る＝データ喪失にはならない）。
     *
     * clearAllTables() はメインスレッドで呼べないため IO ディスパッチャ上で実行する。
     */
    suspend fun clearLocalDataOnSignOut() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
            HealthCheckupDatabase.DEFAULT_ITEM_MASTERS.forEach { master ->
                db.masterDao().upsert(master)
            }
        }
    }

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

    /**
     * Issue #26: ログイン済み状態でアプリを起動したとき、Firestoreとの再同期を行う。
     *
     * `LoginViewModel.signIn()` はサインイン直後の初回復元のみをカバーしており、
     * 既にログイン済みのユーザーがアプリを再起動した場合は `restoreFromFirestore` が
     * 一度も呼ばれず、Web側で追加・編集した記録がAndroidに反映されなかった（US-S01未達）。
     *
     * - 未ログイン（uidが取れない）場合は何もしない。
     * - 1つの HealthRepository インスタンス（＝アプリプロセスの1起動）につき、
     *   実際の同期処理は最初の1回のみ実行する（`startupResyncAttempted` による多重実行防止）。
     *   同じインスタンスに対して複数回呼び出しても2回目以降は即座に返る（冪等）。
     * - Firestore例外・オフライン時のフォールバックは `restoreFromFirestore` に委譲する
     *   （既存のローカルデータを保持し、クラッシュしない）。
     *
     * 呼び出し元（例: MainActivity.onCreate）は UI をブロックしないよう
     * `lifecycleScope.launch` 等のバックグラウンドコルーチンから呼び出すこと。
     */
    suspend fun resyncOnStartupIfNeeded() {
        if (!startupResyncAttempted.compareAndSet(false, true)) return
        val uid = currentUidProvider() ?: return
        restoreFromFirestore(uid)
    }

    /**
     * Issue #34: アカウント削除機能用。
     * 端末の Room DB から全診断記録・全検査項目を削除する。
     * 項目マスター（item_masters）はユーザー個人のデータというより端末の基準値カタログのため対象外
     * （Firestore側の itemMasters は個人データとして削除対象。ローカルとFirestoreでスコープが異なる点に注意）。
     * サインアウトからは呼び出さないこと（共用端末でのRoom DB全削除は別issue #41で対応予定）。
     */
    suspend fun deleteAllLocalHealthData() {
        db.itemDao().deleteAll()
        db.recordDao().deleteAll()
    }

    companion object {
        /**
         * 検査値が基準値外かどうかを判定する。
         * saveRecord() と upsertMaster() の再計算処理で共通利用する。
         * 薬事法対応: 表示ハイライト用途のみで、医療的な合否判定ではない。
         */
        internal fun evaluateAbnormal(value: String?, min: Double?, max: Double?): Boolean {
            val numericValue = value?.toDoubleOrNull() ?: return false
            val belowMin = min?.let { numericValue < it } ?: false
            val aboveMax = max?.let { numericValue > it } ?: false
            return belowMin || aboveMax
        }

        /**
         * 項目マスターの referenceMin / referenceMax が実際に変化したかどうかを判定する。
         * カテゴリ変更・お気に入りトグルのみの更新では false を返し、
         * ExaminationItem の不要な全件再計算・UPDATE を避ける。
         */
        internal fun referenceRangeChanged(previous: ItemMaster?, updated: ItemMaster): Boolean {
            return previous?.referenceMin != updated.referenceMin ||
                previous?.referenceMax != updated.referenceMax
        }
    }
}
