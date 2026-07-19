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
            .addMigrations(HealthCheckupDatabase.MIGRATION_1_2)
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
}
