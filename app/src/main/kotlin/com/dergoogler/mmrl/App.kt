package com.dergoogler.mmrl

import android.app.Application
import android.content.Context
import com.dergoogler.mmrl.app.utils.NotificationUtils
import com.dergoogler.mmrl.network.NetworkUtils
import com.dergoogler.mmrl.platform.PlatformManager
import com.toxicbakery.logging.Arbor
import com.toxicbakery.logging.LogCatSeedling
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    init {
        Arbor.sow(LogCatSeedling())
    }

    override fun onCreate() {
        super.onCreate()
        app = this

        PlatformManager.setHiddenApiExemptions()

        NotificationUtils.init(this)
        NetworkUtils.setCacheDir(cacheDir)
    }

    companion object {
        private lateinit var app: App
        val context: Context get() = app
    }
}