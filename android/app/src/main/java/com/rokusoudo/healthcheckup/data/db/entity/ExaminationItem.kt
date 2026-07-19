package com.rokusoudo.healthcheckup.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 検査項目（明細）エンティティ。
 * ExaminationRecord に紐づく各検査値を表す。
 */
@Entity(
    tableName = "examination_items",
    foreignKeys = [ForeignKey(
        entity = ExaminationRecord::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recordId")]
)
data class ExaminationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val itemName: String,
    val value: String,
    val unit: String,
    val referenceMin: Double?,
    val referenceMax: Double?,
    val isAbnormal: Boolean
)
