package com.rokusoudo.healthcheckup.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemMasterDao {

    @Upsert
    suspend fun upsert(master: ItemMaster)

    @Query("SELECT * FROM item_masters ORDER BY itemName ASC")
    fun getAll(): Flow<List<ItemMaster>>

    @Query("SELECT * FROM item_masters WHERE itemName = :itemName LIMIT 1")
    suspend fun getByName(itemName: String): ItemMaster?

    /** Issue #46: Firestoreへのpushが成功した項目マスターに確認フラグを立てる。 */
    @Query("UPDATE item_masters SET pushedToFirestore = 1 WHERE itemName = :itemName")
    suspend fun markPushed(itemName: String)

    /**
     * Issue #46: 差分ミラー削除。push済み（pushedToFirestore = 1）かつ、
     * 直近の restoreFromFirestore の fetch 結果（[keepItemNames]）に含まれない
     * ＝Firestore側で削除された項目マスターを Room からも削除する。
     * 初期カタログ（DEFAULT_ITEM_MASTERS）への復帰は行わない（#46 未解決の質問2の決定）。
     * push未確認の項目マスターは対象外のため誤って消えない。
     */
    @Query("DELETE FROM item_masters WHERE pushedToFirestore = 1 AND itemName NOT IN (:keepItemNames)")
    suspend fun deleteMirrored(keepItemNames: List<String>)
}
