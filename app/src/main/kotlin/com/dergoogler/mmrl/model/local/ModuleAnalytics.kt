package com.dergoogler.mmrl.model.local

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import timber.log.Timber
import java.io.IOException

data class ModuleAnalytics(
    private val context: Context,
    private val local: List<LocalModule>,
) {
    val totalModules = local.size

    private fun getTotalByState(state: State) = local.filter { it.state == state }.size

    val totalEnabled = getTotalByState(State.ENABLE)
    val totalDisabled = getTotalByState(State.DISABLE)
    val totalUpdated = getTotalByState(State.UPDATE)

    val totalModulesUsageBytes get() = local.sumOf { it.size }

    val totalDeviceStorageBytes: Long
        get() {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            return try {
                val storageStatsManager =
                    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
                val totalBytes = storageStatsManager.getTotalBytes(uuid)
                totalBytes
            } catch (e: IOException) {
                Timber.d("ModuleAnalytics>totalDeviceStorageBytes: $e")
                0L
            }
        }

    val totalStorageUsage = totalModulesUsageBytes.toFloat() / totalDeviceStorageBytes.toFloat()
}
