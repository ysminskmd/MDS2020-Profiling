package com.yandex.academy.mobdev.profiling

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.core.content.ContextCompat
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder().detectAll().penaltyLog().build()
            )
        }

        super.onCreate()

        val config = YandexMetricaConfig.newConfigBuilder("3ba189f5-93eb-49ef-9648-49fdd4365df5")
        YandexMetrica.activate(applicationContext, config.withLogs().build())
        YandexMetrica.enableActivityAutoTracking(this)

        if (isInMainProcess()) {
            if (BuildConfig.DEBUG) {
                Timber.plant(DebugTree())
            } else {
                Timber.plant(AppMetricaTree())
            }
        }
    }

    private fun isInMainProcess(): Boolean {
        val myPid = Process.myPid()
        val activityManager =
            ContextCompat.getSystemService(applicationContext, ActivityManager::class.java)
        return activityManager != null && activityManager.runningAppProcesses.any { process ->
            process.pid == myPid && process.processName == BuildConfig.APPLICATION_ID
        }
    }
}