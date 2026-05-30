package com.example.dn_26

import android.app.Application
import android.util.Log
import timber.log.Timber

/**
 * Application class for global initialization and configuration.
 */
class DroneXApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        Timber.d("DroneX Pro application started")
    }

    /**
     * Custom Timber tree for crash reporting in production.
     */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.ERROR || priority == Log.WARN) {
                // Send to crash reporting service (Firebase Crashlytics, etc.)
            }
        }
    }
}