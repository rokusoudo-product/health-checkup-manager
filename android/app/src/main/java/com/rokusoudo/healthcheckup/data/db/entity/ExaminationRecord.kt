package com.rokusoudo.healthcheckup.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 診断記録（ヘッダー）エンティティ。
 * 1回の健康診断を表す。
 */
@Entity(
    tableName = "examination_records",
    indices = [Index(value = ["remoteId"], unique = true)]
)
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
    val pushedToFirestore: Boolean = false,
    /**
     * Issue #49: Firestoreのドキュメント ID として使うグローバルに一意な ID（UUID v4）。
     * Room の [id] は端末ローカルのAUTOINCREMENT連番のため、同じアカウントで複数端末を使うと
     * 端末間で番号が衝突し、Firestore 上のドキュメントが上書きされ合って記録が消える不具合があった。
     * [remoteId] を Firestore ドキュメント ID・端末間の同一記録の突き合わせキーにすることで、
     * Room の [id] はあくまで端末ローカルの主キーとして自由に採番できるようにする。
     *
     * デフォルト値はKotlin側でランダムなUUIDを生成する（新規作成時に呼び出し側が明示的に
     * 指定しなくても一意性が保たれる）。Room移行（v3→v4）で追加された既存行には、
     * 既存のFirestoreドキュメントとの対応を壊さないよう [id] の文字列表現がそのまま入る
     * （[HealthCheckupDatabase.MIGRATION_3_4] 参照）。
     */
    @ColumnInfo(defaultValue = "")
    val remoteId: String = UUID.randomUUID().toString()
)
