package com.rokusodo.healthcheckup.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 項目マスター（基準値マスター）エンティティ。
 * 検査項目ごとの参考基準値を管理する。
 * 薬事法対応: この値は医学的診断に使用しない。表示・ハイライトのみ。
 */
@Entity(tableName = "item_masters")
data class ItemMaster(
    @PrimaryKey val itemName: String,
    val unit: String,
    val referenceMin: Double?,
    val referenceMax: Double?
)
