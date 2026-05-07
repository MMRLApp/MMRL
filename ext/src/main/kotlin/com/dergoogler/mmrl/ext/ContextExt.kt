package com.dergoogler.mmrl.ext

import android.app.Activity
import android.app.ActivityManager
import android.app.Application.ACTIVITY_SERVICE
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.datetime.LocalDateTime
import java.io.File

val Context.tmpDir
    get() =
        cacheDir
            .resolve("tmp")
            .apply {
                if (!exists()) mkdirs()
            }

fun Context.shareText(
    text: String,
    type: String = "text/plain",
) {
    ShareCompat
        .IntentBuilder(this)
        .setType(type)
        .setText(text)
        .startChooser()
}

fun Context.shareFile(
    file: File,
    type: String = "text/plain",
) {
    ShareCompat
        .IntentBuilder(this)
        .setType(type)
        .addStream(getUriForFile(file))
        .startChooser()
}

fun Context.getUriForFile(file: File): Uri =
    FileProvider.getUriForFile(
        this,
        "$packageName.provider",
        file,
    )

fun Context.isPackageInstalled(target: String): Boolean =
    packageManager.getInstalledApplications(0).find { info -> info.packageName == target } != null

val Context.managerVersion
    get(): Pair<String, Long> {
        return try {
            val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            val versionName = packageInfo.versionName ?: "Unknown"
            Pair(versionName, versionCode)
        } catch (e: Exception) {
            Pair("Unknown", 0)
        }
    }

val Context.logDir get() = cacheDir.resolve("log")

fun Context.deleteLog(name: String) {
    logDir
        .listFiles()
        .orEmpty()
        .forEach {
            if (it.name.startsWith(name) && it.extension == "log") {
                it.delete()
            }
        }
}

fun Context.createLog(name: String) =
    logDir
        .resolve("${name}_${LocalDateTime.now()}.log")
        .apply {
            parentFile?.apply { if (!exists()) mkdirs() }
            createNewFile()
        }

fun Context.getLogPath(name: String) =
    logDir
        .listFiles()
        .orEmpty()
        .find {
            it.name.startsWith(name) && it.extension == "log"
        } ?: createLog(name)

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

val Context.isAppForeground get(): Boolean {
    val mActivityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val l = mActivityManager.runningAppProcesses

    for (info in l) {
        if (info.uid == this.applicationInfo.uid && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            return true
        }
    }

    return false
}
