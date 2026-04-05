package com.rokusodo.healthcheckup

import android.app.Application
import com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusodo.healthcheckup.data.repository.FirestoreRepository
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import com.rokusodo.healthcheckup.ui.notification.NotificationHelper

/**
 * Application クラス。
 * DB・Repository のシングルトンを保持する。
 */
class HealthCheckupApp : Application() {

    val database: HealthCheckupDatabase by lazy {
        HealthCheckupDatabase.getInstance(this)
    }

    val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository()
    }

    val repository: HealthRepository by lazy {
        HealthRepository(database, firestoreRepository)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
