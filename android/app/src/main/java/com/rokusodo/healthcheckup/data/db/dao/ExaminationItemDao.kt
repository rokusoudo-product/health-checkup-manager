package com.rokusodo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ExaminationItemDao {

    @Insert
    suspend fun insertAll(items: List<ExaminationItem>)

    @Query("DELETE FROM examination_items WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)

    @Query("SELECT * FROM examination_items WHERE recordId = :recordId ORDER BY id ASC")
    fun getByRecordId(recordId: Long): Flow<List<ExaminationItem>>
}
