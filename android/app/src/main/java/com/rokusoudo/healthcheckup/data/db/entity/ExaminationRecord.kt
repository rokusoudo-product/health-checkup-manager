package com.rokusoudo.healthcheckup.data.db.entity

import androidx.room.ColumnInfo
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
    val createdAt: Long,    // System.currentTimeMillis()
    /**
     * Issue #46: Firestoreへのpushが確認できているか。
     * - saveRecord() での新規保存直後は false（未確認）
     * - firestoreRepository.saveRecord() が成功した時点、または
     *   restoreFromFirestore() の fetch 結果に含まれていた場合に true へ更新する
     * restoreFromFirestore() の差分ミラー削除は true の行のみを削除対象にすることで、
     * オフライン保存直後などpush未確認のローカル記録を誤って消さないようにする。
     */
    @ColumnInfo(defaultValue = "0")
    val pushedToFirestore: Boolean = false
)
