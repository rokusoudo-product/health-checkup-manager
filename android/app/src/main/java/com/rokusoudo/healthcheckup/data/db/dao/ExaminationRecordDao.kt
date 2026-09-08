package com.rokusoudo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ExaminationRecordDao {

    @Insert
    suspend fun insert(record: ExaminationRecord): Long

    /** Firestore復元時に既存レコードをIDで上書きする */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: ExaminationRecord): Long

    @Delete
    suspend fun delete(record: ExaminationRecord)

    @Query("SELECT * FROM examination_records ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<ExaminationRecord>>

    @Query("SELECT * FROM examination_records WHERE id = :id")
    suspend fun getById(id: Long): ExaminationRecord?

    /** Issue #46: Firestoreへのpushが成功した記録に確認フラグを立てる。 */
    @Query("UPDATE examination_records SET pushedToFirestore = 1 WHERE id = :id")
    suspend fun markPushed(id: Long)

    /**
     * Issue #46: 差分ミラー削除。push済み（pushedToFirestore = 1）かつ、
     * 直近の restoreFromFirestore の fetch 結果（[keepIds]）に含まれない
     * ＝Firestore側で削除された記録を Room からも削除する。
     * push未確認（オフライン保存直後等）の記録は対象外のため誤って消えない。
     * 削除は examination_items へ ON DELETE CASCADE で伝播する。
     */
    @Query("DELETE FROM examination_records WHERE pushedToFirestore = 1 AND id NOT IN (:keepIds)")
    suspend fun deleteMirrored(keepIds: List<Long>)

    /**
     * Issue #34: アカウント削除機能用。端末内の全診断記録を削除する。
     * （サインアウト時には呼ばれない。サインアウトでのRoom DB削除は別issue #41で対応）
     */
    @Query("DELETE FROM examination_records")
    suspend fun deleteAll()
}
