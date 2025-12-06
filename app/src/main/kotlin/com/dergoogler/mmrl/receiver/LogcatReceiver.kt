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
import timber.log.Timber

class LogcatReceiver : MMRLBroadcastReceiver() {
    override suspend fun onBooted(
        context: Context,
        intent: Intent,
    ) {
        context.deleteLog(Logcat.FILE_NAME)
        Timber.i("boot-complete triggered")

        // Restore background services based on user preferences
        val userPreferences = userPreferencesRepository.data.first()

        if (userPreferences.providerServiceEnabled) {
            Timber.i("Restoring ProviderService on boot")
            ProviderService.start(context, userPreferences.workingMode)
        }

        if (userPreferences.repositoryServiceEnabled) {
            Timber.i("Restoring RepositoryService on boot")
            RepositoryService.start(context, userPreferences.autoUpdateReposInterval)
        }

        if (userPreferences.moduleServiceEnabled) {
            Timber.i("Restoring ModuleService on boot")
            ModuleService.start(context, userPreferences.checkModuleUpdatesInterval)
        }
    }
}
