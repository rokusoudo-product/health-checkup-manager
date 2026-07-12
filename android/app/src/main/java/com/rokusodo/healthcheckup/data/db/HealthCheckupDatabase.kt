package com.rokusodo.healthcheckup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rokusodo.healthcheckup.data.db.dao.ExaminationItemDao
import com.rokusodo.healthcheckup.data.db.dao.ExaminationRecordDao
import com.rokusodo.healthcheckup.data.db.dao.ItemMasterDao
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster

@Database(
    entities = [ExaminationRecord::class, ExaminationItem::class, ItemMaster::class],
    version = 1
)
abstract class HealthCheckupDatabase : RoomDatabase() {
    abstract fun recordDao(): ExaminationRecordDao
    abstract fun itemDao(): ExaminationItemDao
    abstract fun masterDao(): ItemMasterDao

    companion object {
        @Volatile private var INSTANCE: HealthCheckupDatabase? = null

        /**
         * 項目マスターの初期データ（一般的な健診基準値）。
         * 薬事法対応: 表示・ハイライトのみに使用し、医療診断を目的としない。
         * 実際のOCR/手入力の項目名と完全一致した場合のみ isAbnormal 判定に使われる。
         */
        private val DEFAULT_ITEM_MASTERS = listOf(
            ItemMaster(itemName = "BMI", unit = "kg/m2", referenceMin = 18.5, referenceMax = 25.0),
            ItemMaster(itemName = "収縮期血圧", unit = "mmHg", referenceMin = null, referenceMax = 129.0),
            ItemMaster(itemName = "拡張期血圧", unit = "mmHg", referenceMin = null, referenceMax = 84.0),
            ItemMaster(itemName = "AST(GOT)", unit = "U/L", referenceMin = null, referenceMax = 30.0),
            ItemMaster(itemName = "ALT(GPT)", unit = "U/L", referenceMin = null, referenceMax = 30.0),
            ItemMaster(itemName = "γ-GTP", unit = "U/L", referenceMin = null, referenceMax = 50.0),
            ItemMaster(itemName = "空腹時血糖", unit = "mg/dL", referenceMin = null, referenceMax = 99.0),
            ItemMaster(itemName = "LDLコレステロール", unit = "mg/dL", referenceMin = null, referenceMax = 139.0),
            ItemMaster(itemName = "HDLコレステロール", unit = "mg/dL", referenceMin = 40.0, referenceMax = null),
            ItemMaster(itemName = "中性脂肪", unit = "mg/dL", referenceMin = null, referenceMax = 149.0)
        )

        private val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DEFAULT_ITEM_MASTERS.forEach { master ->
                    db.execSQL(
                        "INSERT INTO item_masters (itemName, unit, referenceMin, referenceMax) VALUES (?, ?, ?, ?)",
                        arrayOf<Any?>(master.itemName, master.unit, master.referenceMin, master.referenceMax)
                    )
                }
            }
        }

        fun getInstance(context: Context): HealthCheckupDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    HealthCheckupDatabase::class.java,
                    "health_checkup.db"
                )
                    .addCallback(PREPOPULATE_CALLBACK)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
