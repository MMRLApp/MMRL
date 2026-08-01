package com.dergoogler.mmrl.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.dergoogler.mmrl.BuildConfig

private fun PackageManager.getInstalledPackagesCompat(): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getInstalledPackages(
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        getInstalledPackages(0)
    }
}

private fun PackageManager.getLaunchableApps(): List<ResolveInfo> {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        queryIntentActivities(intent, 0)
    }
}

val Context.packages: List<PackageInfo>
    get() {
        if (BuildConfig.IS_GOOGLE_PLAY_BUILD) {
            return packageManager.getLaunchableApps()
                .map { packageManager.getPackageInfo(it.activityInfo.packageName, 0) }
        }

        return packageManager.getInstalledPackagesCompat()
    }