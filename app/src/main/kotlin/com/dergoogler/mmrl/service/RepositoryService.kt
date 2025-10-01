package com.dergoogler.mmrl.service

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
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.database.entity.Repo.Companion.toRepo
import dev.dergoogler.mmrl.compat.worker.MMRLLifecycleService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.hours

class RepositoryService : MMRLLifecycleService() {
    override val title = R.string.notification_name_repository
    override val notificationId = NotificationUtils.NOTIFICATION_ID_REPOSITORY
    override val channelId = NotificationUtils.CHANNEL_ID_REPOSITORY
    override val groupKey = "REPOSITORY_SERVICE_GROUP_KEY"

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

        setForeground()

        val interval = intent.getLongExtra(INTERVAL_KEY, 60000)

        lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    try {
                        val repoDao = database.repoDao()
                        val repos = repoDao.getAll()
                        processRepos(repos)
                    } catch (e: Exception) {
                        sendFailureNotification()
                    }

                    delay(interval.hours)
                }
            }
        }

        return START_STICKY
    }

    private suspend fun processRepos(repos: List<Repo>) {
        coroutineScope {
            var successCount = 0
            var failureCount = 0

            repos.map { repo ->
                async {
                    try {
                        modulesRepository.getRepo(repo.url.toRepo())
                            .onSuccess { successCount++ }
                            .onFailure { failureCount++ }
                    } catch (e: Exception) {
                        Timber.e(e)
                        failureCount++
                    }
                }
            }.awaitAll()

            val repositoriesUpdateMessage =
                applicationContext.resources.getStringArray(R.array.repositories_update_message)
            val randomNotificationMessages =
                repositoriesUpdateMessage.toList().random()

            pushNotification(
                icon = R.drawable.cloud,
                title = applicationContext.getString(R.string.repo_update_service),
                message = randomNotificationMessages.format(successCount, failureCount)
            )
        }
    }

    private fun sendFailureNotification() {
        pushNotification(
            icon = R.drawable.cloud,
            title = applicationContext.getString(R.string.repo_update_service_failed),
            message = applicationContext.getString(R.string.repo_update_service_failed_desc)
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
                    RepositoryService::class.java.name
                )
                putExtra(INTERVAL_KEY, interval)
            }

            context.startForegroundService(intent)
        }


        fun stop(
            context: Context,
        ) {
            val intent = Intent(context, RepositoryService::class.java)
            context.stopService(intent)
        }
    }
}