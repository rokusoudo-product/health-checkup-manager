package com.rokusodo.healthcheckup.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 診断記録（ヘッダー）エンティティ。
 * 1回の健康診断を表す。
 */
@Entity(tableName = "examination_records")
data class ExaminationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,       // "yyyy-MM-dd" 形式
    val facility: String,   // 医療機関名（任意）
    val createdAt: Long     // System.currentTimeMillis()
)
