package com.dergoogler.mmrl.receiver

import android.content.Context
import android.content.Intent
import com.dergoogler.mmrl.ext.deleteLog
import com.dergoogler.mmrl.service.ModuleService
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.service.RepositoryService
import com.dergoogler.mmrl.utils.log.Logcat
import dev.dergoogler.mmrl.compat.worker.MMRLBroadcastReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class LogcatReceiver : MMRLBroadcastReceiver() {
    override suspend fun onBooted(
        context: Context,
        intent: Intent,
    ) {
        context.deleteLog(Logcat.FILE_NAME)
        Timber.i("boot-complete triggered")

        // Restore background services based on user preferences
        try {
            val userPreferences = withTimeout(10000) {
                userPreferencesRepository.data.first()
            }

            if (userPreferences.providerServiceEnabled) {
                try {
                    Timber.i("Restoring ProviderService on boot")
                    ProviderService.start(context, userPreferences.workingMode)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore ProviderService on boot")
                }
            }

            if (userPreferences.repositoryServiceEnabled) {
                try {
                    Timber.i("Restoring RepositoryService on boot")
                    RepositoryService.start(context, userPreferences.autoUpdateReposInterval)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore RepositoryService on boot")
                }
            }

            if (userPreferences.moduleServiceEnabled) {
                try {
                    Timber.i("Restoring ModuleService on boot")
                    ModuleService.start(context, userPreferences.checkModuleUpdatesInterval)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore ModuleService on boot")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read user preferences for service restoration on boot")
        }
    }
}
