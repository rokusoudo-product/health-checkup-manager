package com.rokusoudo.healthcheckup.data.db.dao

/**
 * 経年グラフ用のデータクラス。
 * examination_items と examination_records を JOIN した結果を保持する。
 */
data class ItemTrend(
    val id: Long,
    val recordId: Long,
    val itemName: String,
    val value: String,
    val unit: String,
    val referenceMin: Double?,
    val referenceMax: Double?,
    val isAbnormal: Boolean,
    val recordDate: String   // "yyyy-MM-dd"
)
