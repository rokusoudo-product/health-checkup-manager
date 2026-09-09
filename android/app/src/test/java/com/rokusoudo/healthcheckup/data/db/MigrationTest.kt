package com.rokusoudo.healthcheckup.data.db

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.data.db.entity.ItemCategories
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room v1→v2 Migration のテスト（画面遷移刷新001・Phase2）。
 * v1 スキーマの DB を手組みで作成し、Migration 適用後のスキーマ検証（Room が open 時に実施）と
 * データ保持・マスタ差分投入を確認する。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    /** Room v1 が生成していたスキーマを再現する */
    private fun createV1Database(context: Context) {
        context.deleteDatabase(dbName)
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_records` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, " +
                "`facility` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_items` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, " +
                "`itemName` TEXT NOT NULL, `value` TEXT NOT NULL, `unit` TEXT NOT NULL, " +
                "`referenceMin` REAL, `referenceMax` REAL, `isAbnormal` INTEGER NOT NULL, " +
                "FOREIGN KEY(`recordId`) REFERENCES `examination_records`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_examination_items_recordId` ON `examination_items` (`recordId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `item_masters` " +
                "(`itemName` TEXT NOT NULL, `unit` TEXT NOT NULL, `referenceMin` REAL, `referenceMax` REAL, " +
                "PRIMARY KEY(`itemName`))"
        )
        // v1時代のユーザーデータ: BMI はユーザーが基準値を編集済み（上限24.0）
        db.execSQL("INSERT INTO item_masters (itemName, unit, referenceMin, referenceMax) VALUES ('BMI', 'kg/m2', 18.5, 24.0)")
        db.execSQL("INSERT INTO examination_records (date, facility, createdAt) VALUES ('2025-06-01', 'テスト病院', 1000)")
        db.execSQL(
            "INSERT INTO examination_items (recordId, itemName, value, unit, referenceMin, referenceMax, isAbnormal) " +
                "VALUES (1, 'BMI', '22.0', 'kg/m2', 18.5, 24.0, 0)"
        )
        db.version = 1
        db.close()
    }

    @Test
    fun `v1からv2への移行でデータが保持されマスタが差分投入される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        createV1Database(context)

        val room = Room.databaseBuilder(context, HealthCheckupDatabase::class.java, dbName)
            .addMigrations(
                HealthCheckupDatabase.MIGRATION_1_2,
                HealthCheckupDatabase.MIGRATION_2_3,
                HealthCheckupDatabase.MIGRATION_3_4
            )
            .allowMainThreadQueries()
            .build()
        try {
            runBlocking {
                // ユーザー編集済みの基準値は上書きされず、カテゴリだけ補完される
                val bmi = room.masterDao().getByName("BMI")!!
                assertEquals(24.0, bmi.referenceMax!!, 0.0)
                assertEquals(ItemCategories.BODY, bmi.category)
                assertEquals(false, bmi.isFavorite)

                // 新規マスタ項目が差分投入される（全25項目以上）
                val all = room.masterDao().getAll().first()
                assertTrue("マスタ件数が不足: ${all.size}", all.size >= 25)
                assertEquals(ItemCategories.LIVER, room.masterDao().getByName("AST(GOT)")!!.category)
                assertEquals(ItemCategories.GLUCOSE, room.masterDao().getByName("HbA1c")!!.category)
                assertEquals(ItemCategories.BODY, room.masterDao().getByName("身長")!!.category)

                // 診断記録・検査項目データが保持される
                assertEquals("2025-06-01", room.recordDao().getById(1L)!!.date)
                assertEquals(1, room.itemDao().getByRecordIdOnce(1L).size)
            }
        } finally {
            room.close()
            context.deleteDatabase(dbName)
        }
    }

    /** Room v2 が生成していたスキーマを再現する（category / isFavorite / favoritedAt 追加済み、pushedToFirestore はまだ無い） */
    private fun createV2Database(context: Context) {
        context.deleteDatabase(dbName)
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_records` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, " +
                "`facility` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_items` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, " +
                "`itemName` TEXT NOT NULL, `value` TEXT NOT NULL, `unit` TEXT NOT NULL, " +
                "`referenceMin` REAL, `referenceMax` REAL, `isAbnormal` INTEGER NOT NULL, " +
                "FOREIGN KEY(`recordId`) REFERENCES `examination_records`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_examination_items_recordId` ON `examination_items` (`recordId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `item_masters` " +
                "(`itemName` TEXT NOT NULL, `unit` TEXT NOT NULL, `referenceMin` REAL, `referenceMax` REAL, " +
                "`category` TEXT NOT NULL DEFAULT 'その他', `isFavorite` INTEGER NOT NULL DEFAULT 0, " +
                "`favoritedAt` INTEGER, PRIMARY KEY(`itemName`))"
        )
        // v2時代（Issue #46移行前）に保存済みの記録・項目マスター。pushedToFirestoreの概念自体が無かった。
        db.execSQL(
            "INSERT INTO item_masters (itemName, unit, referenceMin, referenceMax, category, isFavorite, favoritedAt) " +
                "VALUES ('LDLコレステロール', 'mg/dL', NULL, 139.0, '脂質', 1, 12345)"
        )
        db.execSQL("INSERT INTO examination_records (date, facility, createdAt) VALUES ('2025-06-01', 'テスト病院', 1000)")
        db.execSQL(
            "INSERT INTO examination_items (recordId, itemName, value, unit, referenceMin, referenceMax, isAbnormal) " +
                "VALUES (1, 'LDLコレステロール', '150', 'mg/dL', NULL, 139.0, 1)"
        )
        db.version = 2
        db.close()
    }

    /**
     * Room v2→v3 Migration のテスト（Issue #46）。
     * pushedToFirestore カラムが追加され、既存行は false（未確認）が既定値になること、
     * 既存データ自体は一切失われないことを確認する。
     */
    @Test
    fun `v2からv3への移行で既存データは保持されpushedToFirestoreはfalseが既定になる`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        createV2Database(context)

        val room = Room.databaseBuilder(context, HealthCheckupDatabase::class.java, dbName)
            .addMigrations(HealthCheckupDatabase.MIGRATION_2_3, HealthCheckupDatabase.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        try {
            runBlocking {
                // 既存の項目マスター・診断記録・検査項目は失われない
                val master = room.masterDao().getByName("LDLコレステロール")!!
                assertEquals(139.0, master.referenceMax!!, 0.0)
                assertEquals(true, master.isFavorite)
                // Issue #46: 移行前のデータは push 状況が不明なため、安全側（未確認=false）に倒す
                assertEquals(false, master.pushedToFirestore)

                val record = room.recordDao().getById(1L)!!
                assertEquals("2025-06-01", record.date)
                assertEquals(false, record.pushedToFirestore)
                assertEquals(1, room.itemDao().getByRecordIdOnce(1L).size)
            }
        } finally {
            room.close()
            context.deleteDatabase(dbName)
        }
    }

    /** Room v3 が生成していたスキーマを再現する（pushedToFirestore追加済み、remoteId はまだ無い） */
    private fun createV3Database(context: Context) {
        context.deleteDatabase(dbName)
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_records` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, " +
                "`facility` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`pushedToFirestore` INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `examination_items` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, " +
                "`itemName` TEXT NOT NULL, `value` TEXT NOT NULL, `unit` TEXT NOT NULL, " +
                "`referenceMin` REAL, `referenceMax` REAL, `isAbnormal` INTEGER NOT NULL, " +
                "FOREIGN KEY(`recordId`) REFERENCES `examination_records`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_examination_items_recordId` ON `examination_items` (`recordId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `item_masters` " +
                "(`itemName` TEXT NOT NULL, `unit` TEXT NOT NULL, `referenceMin` REAL, `referenceMax` REAL, " +
                "`category` TEXT NOT NULL DEFAULT 'その他', `isFavorite` INTEGER NOT NULL DEFAULT 0, " +
                "`favoritedAt` INTEGER, `pushedToFirestore` INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(`itemName`))"
        )
        // v3時代（Issue #49移行前）に保存済みの記録。Firestoreのドキュメント ID はこのRoom idの
        // 文字列表現がそのまま使われていた（FirestoreRepository.saveRecord の旧実装）。
        db.execSQL(
            "INSERT INTO examination_records (id, date, facility, createdAt, pushedToFirestore) " +
                "VALUES (7, '2025-06-01', 'テスト病院', 1000, 1)"
        )
        db.execSQL(
            "INSERT INTO examination_items (recordId, itemName, value, unit, referenceMin, referenceMax, isAbnormal) " +
                "VALUES (7, 'LDLコレステロール', '150', 'mg/dL', NULL, 139.0, 1)"
        )
        db.version = 3
        db.close()
    }

    /**
     * Room v3→v4 Migration のテスト（Issue #49）。
     *
     * - remoteId カラムが追加されること
     * - 既存行（移行前に作成された記録）には remoteId として id の文字列表現がそのまま入り、
     *   既存のFirestoreドキュメントとの対応が壊れないこと（一括移行はしない共存方式）
     * - 既存データ（診断記録・検査項目）自体は一切失われないこと
     */
    @Test
    fun `v3からv4への移行で既存行のremoteIdにはidの文字列表現が入る`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        createV3Database(context)

        val room = Room.databaseBuilder(context, HealthCheckupDatabase::class.java, dbName)
            .addMigrations(HealthCheckupDatabase.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        try {
            runBlocking {
                val record = room.recordDao().getById(7L)!!
                assertEquals("2025-06-01", record.date)
                // Issue #49: 既存行のremoteIdには、移行前のFirestoreドキュメントID
                // （Roomのid.toString()）がそのまま入り、既存ドキュメントとの対応を壊さない
                assertEquals("7", record.remoteId)
                assertEquals(true, record.pushedToFirestore)

                // remoteIdキーでも同じ行が引けること（restoreFromFirestoreの突き合わせに使う）
                val byRemoteId = room.recordDao().getByRemoteId("7")
                assertEquals(7L, byRemoteId!!.id)

                // 既存データ自体は失われない
                assertEquals(1, room.itemDao().getByRecordIdOnce(7L).size)
            }
        } finally {
            room.close()
            context.deleteDatabase(dbName)
        }
    }
}
