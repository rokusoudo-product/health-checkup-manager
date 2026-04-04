package com.rokusodo.healthcheckup.data.repository

import com.rokusodo.healthcheckup.OcrItem
import com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusodo.healthcheckup.data.db.dao.ItemTrend
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.Flow

/**
 * 健康診断データの Repository。
 * Room DB への読み書きを一元管理する。
 * 薬事法対応: isAbnormal は表示のみに使用し、医療診断を目的としない。
 */
class HealthRepository(private val db: HealthCheckupDatabase) {

    /**
     * OCR結果を診断記録として保存する。
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

        return recordId
    }

    fun getAllRecords(): Flow<List<ExaminationRecord>> = db.recordDao().getAll()

    fun getItemsForRecord(recordId: Long): Flow<List<ExaminationItem>> =
        db.itemDao().getByRecordId(recordId)

    fun getAllMasters(): Flow<List<ItemMaster>> = db.masterDao().getAll()

    suspend fun upsertMaster(master: ItemMaster) = db.masterDao().upsert(master)

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
}
