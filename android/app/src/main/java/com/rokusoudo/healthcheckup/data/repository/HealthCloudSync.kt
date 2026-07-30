package com.rokusoudo.healthcheckup.data.repository

import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster

/**
 * HealthRepository から見たクラウド同期先の抽象。
 * 実装は FirestoreRepository。テストではフェイク実装に差し替え可能にするための境界。
 */
interface HealthCloudSync {
    suspend fun saveRecord(uid: String, record: ExaminationRecord, items: List<ExaminationItem>)
    suspend fun saveItemMaster(uid: String, master: ItemMaster)
    suspend fun fetchRecords(uid: String): List<Pair<ExaminationRecord, List<ExaminationItem>>>
    suspend fun fetchItemMasters(uid: String): List<ItemMaster>
}
