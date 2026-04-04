package com.rokusodo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemMasterDao {

    @Upsert
    suspend fun upsert(master: ItemMaster)

    @Query("SELECT * FROM item_masters ORDER BY itemName ASC")
    fun getAll(): Flow<List<ItemMaster>>

    @Query("SELECT * FROM item_masters WHERE itemName = :itemName LIMIT 1")
    suspend fun getByName(itemName: String): ItemMaster?
}
