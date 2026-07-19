package com.rokusoudo.healthcheckup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rokusoudo.healthcheckup.data.db.dao.ExaminationItemDao
import com.rokusoudo.healthcheckup.data.db.dao.ExaminationRecordDao
import com.rokusoudo.healthcheckup.data.db.dao.ItemMasterDao
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusoudo.healthcheckup.data.db.entity.ItemCategories
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster

@Database(
    entities = [ExaminationRecord::class, ExaminationItem::class, ItemMaster::class],
    version = 2
)
abstract class HealthCheckupDatabase : RoomDatabase() {
    abstract fun recordDao(): ExaminationRecordDao
    abstract fun itemDao(): ExaminationItemDao
    abstract fun masterDao(): ItemMasterDao

    companion object {
        @Volatile private var INSTANCE: HealthCheckupDatabase? = null

        /**
         * 項目マスターの初期データ（法定健診レベル・一般的な健診基準値。刷新001 Q1で拡充）。
         * 薬事法対応: 表示・ハイライトのみに使用し、医療診断を目的としない。
         * 性別依存項目（Hb・腹囲・クレアチニン等）は基準値なし（項目マスター画面で編集可能）。
         */
        internal val DEFAULT_ITEM_MASTERS = listOf(
            // 身体計測
            ItemMaster("身長", "cm", null, null, ItemCategories.BODY),
            ItemMaster("体重", "kg", null, null, ItemCategories.BODY),
            ItemMaster("BMI", "kg/m2", 18.5, 25.0, ItemCategories.BODY),
            ItemMaster("腹囲", "cm", null, null, ItemCategories.BODY),
            // 血圧
            ItemMaster("収縮期血圧", "mmHg", null, 129.0, ItemCategories.BLOOD_PRESSURE),
            ItemMaster("拡張期血圧", "mmHg", null, 84.0, ItemCategories.BLOOD_PRESSURE),
            // 血液一般
            ItemMaster("血色素量(Hb)", "g/dL", null, null, ItemCategories.BLOOD),
            ItemMaster("赤血球数", "万/μL", null, null, ItemCategories.BLOOD),
            ItemMaster("白血球数", "/μL", 3100.0, 8400.0, ItemCategories.BLOOD),
            ItemMaster("血小板数", "万/μL", 14.5, 32.9, ItemCategories.BLOOD),
            // 脂質
            ItemMaster("LDLコレステロール", "mg/dL", null, 139.0, ItemCategories.LIPID),
            ItemMaster("HDLコレステロール", "mg/dL", 40.0, null, ItemCategories.LIPID),
            ItemMaster("中性脂肪", "mg/dL", null, 149.0, ItemCategories.LIPID),
            // 肝機能
            ItemMaster("AST(GOT)", "U/L", null, 30.0, ItemCategories.LIVER),
            ItemMaster("ALT(GPT)", "U/L", null, 30.0, ItemCategories.LIVER),
            ItemMaster("γ-GTP", "U/L", null, 50.0, ItemCategories.LIVER),
            // 腎機能
            ItemMaster("クレアチニン", "mg/dL", null, null, ItemCategories.KIDNEY),
            ItemMaster("eGFR", "mL/min/1.73m2", 60.0, null, ItemCategories.KIDNEY),
            ItemMaster("尿酸", "mg/dL", null, 7.0, ItemCategories.KIDNEY),
            // 糖代謝
            ItemMaster("空腹時血糖", "mg/dL", null, 99.0, ItemCategories.GLUCOSE),
            ItemMaster("HbA1c", "%", null, 5.5, ItemCategories.GLUCOSE),
            // 尿検査（定性値のためグラフ対象外）
            ItemMaster("尿蛋白", "", null, null, ItemCategories.URINE),
            ItemMaster("尿糖", "", null, null, ItemCategories.URINE),
            // その他（グラフ対象外）
            ItemMaster("視力(右)", "", null, null, ItemCategories.OTHER),
            ItemMaster("視力(左)", "", null, null, ItemCategories.OTHER)
        )

        /**
         * v1→v2: item_masters に category / isFavorite / favoritedAt を追加し、
         * 既存行へカテゴリを補完・新規マスタ項目を差分投入する。
         * 初期シードは onCreate 時にしか走らないため、既存端末にはこの Migration で反映する。
         * ユーザーが編集済みの基準値・単位は上書きしない（category の UPDATE と INSERT OR IGNORE のみ）。
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE item_masters ADD COLUMN category TEXT NOT NULL DEFAULT 'その他'")
                db.execSQL("ALTER TABLE item_masters ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE item_masters ADD COLUMN favoritedAt INTEGER")
                DEFAULT_ITEM_MASTERS.forEach { master ->
                    db.execSQL(
                        "UPDATE item_masters SET category = ? WHERE itemName = ?",
                        arrayOf<Any?>(master.category, master.itemName)
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO item_masters " +
                            "(itemName, unit, referenceMin, referenceMax, category, isFavorite, favoritedAt) " +
                            "VALUES (?, ?, ?, ?, ?, 0, NULL)",
                        arrayOf<Any?>(
                            master.itemName, master.unit,
                            master.referenceMin, master.referenceMax, master.category
                        )
                    )
                }
            }
        }

        private val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DEFAULT_ITEM_MASTERS.forEach { master ->
                    db.execSQL(
                        "INSERT INTO item_masters (itemName, unit, referenceMin, referenceMax, category) " +
                            "VALUES (?, ?, ?, ?, ?)",
                        arrayOf<Any?>(
                            master.itemName, master.unit,
                            master.referenceMin, master.referenceMax, master.category
                        )
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
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
