package com.rokusodo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord
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
}
