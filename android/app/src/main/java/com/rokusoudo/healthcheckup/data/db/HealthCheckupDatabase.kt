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
    version = 4
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

        /**
         * v2→v3（Issue #46）: examination_records / item_masters に
         * pushedToFirestore（Firestoreへpush済みか確認できているか）を追加する。
         *
         * 既存行の既定値は false（未確認扱い）とする。
         *
         * 設計判断（安全側に倒す理由）:
         * 既存インストールの各行が実際に Firestore への push に成功していたかどうかは
         * これまで記録されておらず、区別する手段がない。ここで true を既定にすると、
         * 「実は保存時にオフラインで push が失敗し、ローカルにしか存在しない記録・マスター」が
         * 次回の restoreFromFirestore の差分ミラー削除で誤って削除されてしまう
         * （＝健康記録という機微データの消失）リスクがある。
         * false を既定にしておけば、その行が実際に Firestore 上にも存在する場合は
         * 次回 restoreFromFirestore の fetch で該当ドキュメントが返り、
         * pushedToFirestore=true に更新されたうえで通常のミラー削除対象に入るため実害はない。
         * 一方、true を既定にした場合に起こり得る「データ消失」の方が
         * false を既定にした場合に起こり得る「削除されないまま残り続ける（復活はしない）」より
         * 明らかに深刻なため、false を選択する。
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE examination_records ADD COLUMN pushedToFirestore INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE item_masters ADD COLUMN pushedToFirestore INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v3→v4（Issue #49）: examination_records に、Firestoreドキュメント ID として使う
         * グローバルに一意な [ExaminationRecord.remoteId] を追加する。
         *
         * 背景: これまで Firestore のドキュメント ID には Room の AUTOINCREMENT 連番
         * （[ExaminationRecord.id] の文字列表現）をそのまま使っていた。この連番は端末ローカルの
         * 採番空間のため、同じ Google アカウントで Android 端末を2台使うと、両方の端末が
         * 独立に同じ番号（例: id=2）を採番し得る。その結果 Firestore 上で同じドキュメント ID に
         * 書き込みが競合し、`set()`（mergeなし）により片方の健診記録が完全に上書きされ消失する
         * （Issue #49 本文の再現シナリオ）。
         *
         * 対応方針（Issue #49「未解決の質問」への回答・共存方式を採用）:
         * - 既存の数値IDドキュメントを新IDへ書き換える一括移行は行わない。
         *   Firestore上の全記録を読み直して新IDで書き直し旧IDを削除する操作は、
         *   健診データという機微データに対して途中失敗時の復旧が困難なため。
         * - 代わりに、既存行の [ExaminationRecord.remoteId] には現在の [ExaminationRecord.id] の
         *   文字列表現をそのまま入れる。これにより既存のFirestoreドキュメントとの対応
         *   （ドキュメントID＝移行前のid.toString()）は変更後も壊れない。
         * - 新規作成される記録のみ、Kotlin側のデフォルト値（UUID.randomUUID()）により
         *   グローバルに一意なIDが採番される（[FirestoreRepository.saveRecord] 参照）。
         * - この結果、Firestore上のドキュメントIDは「数値ID（移行前からの既存記録）」と
         *   「UUID（移行後の新規記録）」の2形式が混在するが、[remoteId] をキーにすれば
         *   Android側の読み出し・突き合わせは形式によらず統一的に扱える。
         *
         * UNIQUE インデックスを付与し、同一端末内で remoteId が重複しないことをDBレベルでも担保する。
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE examination_records ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE examination_records SET remoteId = CAST(id AS TEXT)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_examination_records_remoteId " +
                        "ON examination_records(remoteId)"
                )
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
