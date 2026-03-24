package com.example.ngdtechsupport

import android.app.Application
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

class NgdTechSupportApp : Application() {

    companion object {
        private const val TAG = "NgdTechSupportApp"
    }

    override fun onCreate() {
        super.onCreate()

        val monitoringEnabled = BuildConfig.ENABLE_FIREBASE_MONITORING
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(monitoringEnabled)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = monitoringEnabled

        val appCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            Log.d(TAG, "Firebase App Check initialized with Debug provider")
        } else {
            appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            Log.d(TAG, "Firebase App Check initialized with Play Integrity provider")
        }
    }
}
