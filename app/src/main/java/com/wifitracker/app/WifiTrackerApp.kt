package com.wifitracker.app

import android.app.Application
import com.wifitracker.app.data.AppDatabase

class WifiTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
