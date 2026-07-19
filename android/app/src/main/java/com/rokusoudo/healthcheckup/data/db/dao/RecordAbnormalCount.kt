package com.rokusoudo.healthcheckup.data.db.dao

/**
 * 診断記録ごとの基準値外（isAbnormal=true）項目数。
 * 診断記録一覧の状態バッジ表示に使用する。
 * 薬事法対応: 表示のみに使用し、医療診断を目的としない。
 */
data class RecordAbnormalCount(
    val recordId: Long,
    val count: Int
)
