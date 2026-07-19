package com.rokusoudo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ExaminationItemDao {

    @Insert
    suspend fun insertAll(items: List<ExaminationItem>)

    @Query("DELETE FROM examination_items WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)

    @Query("SELECT * FROM examination_items WHERE recordId = :recordId ORDER BY id ASC")
    fun getByRecordId(recordId: Long): Flow<List<ExaminationItem>>

    /**
     * 特定の項目名を持つ検査項目を日付昇順で取得（グラフ用）
     */
    @Query("""
        SELECT ei.id, ei.recordId, ei.itemName, ei.value, ei.unit,
               ei.referenceMin, ei.referenceMax, ei.isAbnormal,
               er.date as recordDate
        FROM examination_items ei
        INNER JOIN examination_records er ON ei.recordId = er.id
        WHERE ei.itemName = :itemName
        ORDER BY er.date ASC
    """)
    suspend fun getItemTrendByName(itemName: String): List<ItemTrend>

    /**
     * isAbnormal = true の全検査項目を新しい順で取得
     */
    @Query("SELECT * FROM examination_items WHERE isAbnormal = 1 ORDER BY id DESC")
    fun getAllAbnormalItems(): Flow<List<ExaminationItem>>

    /**
     * 指定した recordId の検査項目を一度だけ取得（通知判定用）
     */
    @Query("SELECT * FROM examination_items WHERE recordId = :recordId ORDER BY id ASC")
    suspend fun getByRecordIdOnce(recordId: Long): List<ExaminationItem>

    /**
     * 記録ごとの基準値外項目数を取得（診断記録一覧の状態バッジ用）
     */
    @Query("SELECT recordId, COUNT(*) as count FROM examination_items WHERE isAbnormal = 1 GROUP BY recordId")
    fun getAbnormalCountsByRecord(): Flow<List<RecordAbnormalCount>>
}
