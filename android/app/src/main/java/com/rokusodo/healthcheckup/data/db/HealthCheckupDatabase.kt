package com.rokusodo.healthcheckup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

        fun getInstance(context: Context): HealthCheckupDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    HealthCheckupDatabase::class.java,
                    "health_checkup.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
