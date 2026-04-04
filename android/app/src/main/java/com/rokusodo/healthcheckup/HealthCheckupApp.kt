package com.rokusodo.healthcheckup

import android.app.Application
import com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusodo.healthcheckup.data.repository.HealthRepository

/**
 * Application クラス。
 * DB・Repository のシングルトンを保持する。
 */
class HealthCheckupApp : Application() {

    val database: HealthCheckupDatabase by lazy {
        HealthCheckupDatabase.getInstance(this)
    }

    val repository: HealthRepository by lazy {
        HealthRepository(database)
    }
}
