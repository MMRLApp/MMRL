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
            service.getSystemService("package"),
        )
    }

    fun getApplicationInfo(
        packageName: String,
        flags: Int,
        userId: Int,
    ): ApplicationInfo =
        try {
            if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "getApplicationInfo",
                        String::class.java,
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags.toLong(), userId) as ApplicationInfo
            } else {
                packageManager::class.java
                    .getMethod(
                        "getApplicationInfo",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags, userId) as ApplicationInfo
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting application info for $packageName", e)
            throw e
        }

    fun getPackageInfo(
        packageName: String,
        flags: Int,
        userId: Int,
    ): PackageInfo =
        try {
            if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "getPackageInfo",
                        String::class.java,
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags.toLong(), userId) as PackageInfo
            } else {
                packageManager::class.java
                    .getMethod(
                        "getPackageInfo",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags, userId) as PackageInfo
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package info for $packageName", e)
            throw e
        }

    fun getPackageUid(
        packageName: String,
        flags: Int,
        userId: Int,
    ): Int =
        try {
            if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "getPackageUid",
                        String::class.java,
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags.toLong(), userId) as Int
            } else {
                packageManager::class.java
                    .getMethod(
                        "getPackageUid",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, packageName, flags, userId) as Int
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package UID for $packageName", e)
            throw e
        }

    fun getInstalledPackages(
        flags: Int,
        userId: Int,
    ): List<PackageInfo> =
        try {
            val result = if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "getInstalledPackages",
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, flags.toLong(), userId)
            } else {
                packageManager::class.java
                    .getMethod(
                        "getInstalledPackages",
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, flags, userId)
            }
            // Result is a ParceledListSlice, we need to get the list from it
            (result::class.java.getMethod("getList").invoke(result) as List<*>)
                .filterIsInstance<PackageInfo>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed packages", e)
            emptyList()
        }

    fun getInstalledApplications(
        flags: Int,
        userId: Int,
    ): List<ApplicationInfo> =
        try {
            val result = if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "getInstalledApplications",
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, flags.toLong(), userId)
            } else {
                packageManager::class.java
                    .getMethod(
                        "getInstalledApplications",
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, flags, userId)
            }
            // Result is a ParceledListSlice, we need to get the list from it
            (result::class.java.getMethod("getList").invoke(result) as List<*>)
                .filterIsInstance<ApplicationInfo>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed applications", e)
            emptyList()
        }

    fun getInstalledPackagesAll(
        userManager: HiddenUserManager,
        flags: Int = 0,
    ): List<PackageInfo> =
        try {
            val packages = mutableListOf<PackageInfo>()
            val userProfileIds = userManager.getUserProfiles().map { it.hashCode() }

            packages.addAll(
                userProfileIds.flatMap {
                    getInstalledPackages(flags, it)
                },
            )

            packages
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledPackagesAll", e)
            getInstalledPackages(0, userManager.myUserId)
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledPackagesAll", e)
            emptyList()
        }

    fun queryIntentActivities(
        intent: Intent,
        resolvedType: String?,
        flags: Int,
        userId: Int,
    ): List<ResolveInfo> =
        try {
            val result = if (BuildCompat.atLeastT) {
                packageManager::class.java
                    .getMethod(
                        "queryIntentActivities",
                        Intent::class.java,
                        String::class.java,
                        Long::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, intent, resolvedType, flags.toLong(), userId)
            } else {
                packageManager::class.java
                    .getMethod(
                        "queryIntentActivities",
                        Intent::class.java,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    ).invoke(packageManager, intent, resolvedType, flags, userId)
            }
            // Result is a ParceledListSlice, we need to get the list from it
            (result::class.java.getMethod("getList").invoke(result) as List<*>)
                .filterIsInstance<ResolveInfo>()
        } catch (e: Exception) {
            Log.e(TAG, "Error querying intent activities", e)
            emptyList()
        }

    fun getPackagesForUid(uid: Int): List<String> =
        try {
            val result = packageManager::class.java
                .getMethod("getPackagesForUid", Int::class.javaPrimitiveType)
                .invoke(packageManager, uid)
            (result as? Array<*>)?.mapNotNull { it as? String } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting packages for UID $uid", e)
            emptyList()
        }

    fun getLaunchIntentForPackage(
        packageName: String,
        userId: Int,
    ): Intent? {
        val intentToResolve = Intent(Intent.ACTION_MAIN)
        intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER)
        intentToResolve.setPackage(packageName)

        val ris: List<ResolveInfo> =
            queryIntentActivities(
                intentToResolve,
                null,
                0,
                userId,
            )

        if (ris.isEmpty()) {
            return null
        }

        val intent = Intent(intentToResolve)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClassName(
            ris[0].activityInfo.packageName,
            ris[0].activityInfo.name,
        )

        return intent
    }

    fun clearApplicationProfileData(packageName: String) {
        try {
            packageManager::class.java
                .getMethod("clearApplicationProfileData", String::class.java)
                .invoke(packageManager, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing application profile data for $packageName", e)
        }
    }

    fun performDexOpt(packageName: String): Boolean =
        try {
            packageManager::class.java
                .getMethod(
                    "performDexOptMode",
                    String::class.java,
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                ).invoke(
                    packageManager,
                    packageName,
                    SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false),
                    SystemProperties.get("pm.dexopt.install", "speed-profile"),
                    true,
                    true,
                    null,
                ) as Boolean
        } catch (e: Exception) {
            Log.e(TAG, "Error performing dex opt for $packageName", e)
            false
        }

    companion object {
        const val TAG = "HiddenPackageManager"
    }
}
