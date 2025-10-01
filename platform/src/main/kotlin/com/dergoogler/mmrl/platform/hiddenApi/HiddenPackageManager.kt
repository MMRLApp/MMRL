@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.dergoogler.mmrl.platform.hiddenApi

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.IPackageManager
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.os.SystemProperties
import android.util.Log
import com.dergoogler.mmrl.compat.BuildCompat
import com.dergoogler.mmrl.platform.PlatformManager.getSystemService
import com.dergoogler.mmrl.platform.stub.IServiceManager

class HiddenPackageManager(
    private val service: IServiceManager,
) {
    private val packageManager by lazy {
        IPackageManager.Stub.asInterface(
            service.getSystemService("package")
        )
    }

    fun getApplicationInfo(packageName: String, flags: Int, userId: Int): ApplicationInfo {
        return if (BuildCompat.atLeastT) {
            packageManager.getApplicationInfo(packageName, flags.toLong(), userId)
        } else {
            packageManager.getApplicationInfo(packageName, flags, userId)
        }
    }

    fun getPackageInfo(packageName: String, flags: Int, userId: Int): PackageInfo {
        return if (BuildCompat.atLeastT) {
            packageManager.getPackageInfo(packageName, flags.toLong(), userId)
        } else {
            packageManager.getPackageInfo(packageName, flags, userId)
        }
    }

    fun getPackageUid(packageName: String, flags: Int, userId: Int): Int {
        return if (BuildCompat.atLeastT) {
            packageManager.getPackageUid(packageName, flags.toLong(), userId)
        } else {
            packageManager.getPackageUid(packageName, flags, userId)
        }
    }

    fun getInstalledPackages(flags: Int, userId: Int): List<PackageInfo> {
        return if (BuildCompat.atLeastT) {
            packageManager.getInstalledPackages(flags.toLong(), userId)
        } else {
            packageManager.getInstalledPackages(flags, userId)
        }.list
    }

    fun getInstalledApplications(flags: Int, userId: Int): List<ApplicationInfo> {
        return if (BuildCompat.atLeastT) {
            packageManager.getInstalledApplications(flags.toLong(), userId)
        } else {
            packageManager.getInstalledApplications(flags, userId)
        }.list
    }

    fun getInstalledPackagesAll(userManager: HiddenUserManager, flags: Int = 0): List<PackageInfo> {
        return try {
            val packages = mutableListOf<PackageInfo>()
            val userProfileIds = userManager.getUserProfiles().map { it.hashCode() }

            packages.addAll(userProfileIds.flatMap {
                getInstalledPackages(flags, it)
            })

            packages
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledPackagesAll", e)
            getInstalledPackages(0, userManager.myUserId)
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledPackagesAll", e)
            emptyList()
        }
    }

    fun queryIntentActivities(
        intent: Intent,
        resolvedType: String?,
        flags: Int,
        userId: Int,
    ): List<ResolveInfo> {
        return if (BuildCompat.atLeastT) {
            packageManager.queryIntentActivities(intent, resolvedType, flags.toLong(), userId)
        } else {
            packageManager.queryIntentActivities(intent, resolvedType, flags, userId)
        }.list
    }

    fun getPackagesForUid(uid: Int): List<String> {
        return packageManager.getPackagesForUid(uid).toList()
    }

    fun getLaunchIntentForPackage(packageName: String, userId: Int): Intent? {
        val intentToResolve = Intent(Intent.ACTION_MAIN)
        intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER)
        intentToResolve.setPackage(packageName)

        val ris: List<ResolveInfo> = queryIntentActivities(
            intentToResolve, null, 0, userId
        )

        if (ris.isEmpty()) {
            return null
        }

        val intent = Intent(intentToResolve)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClassName(
            ris[0].activityInfo.packageName,
            ris[0].activityInfo.name
        )

        return intent
    }

    fun clearApplicationProfileData(packageName: String) {
        packageManager.clearApplicationProfileData(packageName)
    }

    fun performDexOpt(packageName: String): Boolean {
        return packageManager.performDexOptMode(
            packageName,
            SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false),
            SystemProperties.get("pm.dexopt.install", "speed-profile"),
            true,
            true,
            null
        )
    }

    companion object {
        const val TAG = "HiddenPackageManager"
    }
}