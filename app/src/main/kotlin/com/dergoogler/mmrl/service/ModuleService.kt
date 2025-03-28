package com.dergoogler.mmrl.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.utils.NotificationUtils
import com.dergoogler.mmrl.database.entity.local.LocalModuleEntity
import com.dergoogler.mmrl.database.entity.online.OnlineModuleEntity
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.stub.IRepoManager
import com.dergoogler.mmrl.ui.activity.MainActivity
import dev.dergoogler.mmrl.compat.worker.MMRLLifecycleService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.time.Duration.Companion.hours

class ModuleService : MMRLLifecycleService() {
    override val title = R.string.notification_name_module
    override val notificationId = NotificationUtils.NOTIFICATION_ID_MODULE
    override val channelId = NotificationUtils.CHANNEL_ID_MODULE
    override val groupKey = "MODULE_SERVICE_GROUP_KEY"

    override fun onCreate() {
        super.onCreate()
        isActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            Timber.w("onStartCommand: intent is null")
            return START_NOT_STICKY
        }

        val interval = intent.getLongExtra(INTERVAL_KEY, 60000)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    try {
                        checkForUpdatesAndNotify()
                    } catch (e: Exception) {
                        Timber.e(e, "Update check failed")
                    }

                    delay(interval.hours)
                }
            }
        }

        return START_STICKY
    }

    private suspend fun checkForUpdatesAndNotify() = withContext(Dispatchers.IO) {
        val onlineModules = fetchOnlineModules()
        val localModules = database.localDao().getAll()
        val onlineModulesOrderedByNewestMap = onlineModules.groupBy { it.id }
                .mapValues { module -> module.value.sortedByDescending { it.versionCode } }

        localModules.forEach { localModule ->
            onlineModulesOrderedByNewestMap[localModule.id]?.getOrNull(0)?.let { newestOnlineModule ->
                if (isNewerVersion(newestOnlineModule, localModule)) {
                    println("New version found! " + newestOnlineModule)
                }
            }
        }
    }

    private suspend fun fetchOnlineModules(): List<OnlineModuleEntity> =
        withContext(Dispatchers.IO) {
            val onlineModules = mutableListOf<OnlineModuleEntity>()
            database.repoDao().getAll().forEach { repo ->
                val repoManager = IRepoManager.build(repo.url)
                try {
                    val response = repoManager.modules.execute()
                    response.body()?.modules?.let { modulesJson ->
                        onlineModules.addAll(modulesJson.map { module ->
                            OnlineModuleEntity(module, repo.url, Blacklist.EMPTY)
                        })
                    } ?: Timber.e("No data found for repo: ${repo.url}")
                } catch (e: Exception) {
                    Timber.e(e, "Error while fetching repo: ${repo.url}")
                }
            }
            onlineModules
        }

    private fun isNewerVersion(
        onlineModule: OnlineModuleEntity,
        localModule: LocalModuleEntity
    ): Boolean = onlineModule.versionCode > localModule.versionCode

    private fun sendUpdateNotification(
        localModule: LocalModuleEntity,
        onlineModule: OnlineModuleEntity
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        pushNotification(
            id = localModule.id.hashCode(),
            icon = R.drawable.file_3d,
            title = applicationContext.getString(R.string.has_a_new_update, localModule.name),
            message = applicationContext.getString(
                R.string.update_available_from_to,
                localModule.version, localModule.versionCode,
                onlineModule.version, onlineModule.versionCode
            ),
            pendingIntent = pendingIntent
        )
    }

    companion object {
        var isActive by mutableStateOf(false)
            private set
        private const val INTERVAL_KEY = "INTERVAL"

        fun start(
            context: Context,
            interval: Long,
        ) {
            val intent = Intent().apply {
                component = ComponentName(
                    context.packageName,
                    ModuleService::class.java.name
                )
                putExtra(INTERVAL_KEY, interval)
            }
            context.startService(intent)
        }


        fun stop(
            context: Context,
        ) {
            val intent = Intent(context, ModuleService::class.java)
            context.stopService(intent)
        }
    }
}
