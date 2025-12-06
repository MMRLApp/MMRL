package com.dergoogler.mmrl.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.app.utils.NotificationUtils
import com.dergoogler.mmrl.compat.MediaStoreCompat.createDownloadUri
import com.dergoogler.mmrl.compat.NetworkCompat
import com.dergoogler.mmrl.compat.PermissionCompat
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.ext.parcelable
import dagger.hilt.android.AndroidEntryPoint
import dev.dergoogler.mmrl.compat.BuildCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : LifecycleService() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val tasks = mutableListOf<TaskItem>()

    init {
        lifecycleScope.launch {
            while (isActive) {
                delay(10_000L)
                if (tasks.isEmpty()) stopSelf()
            }
        }

        progressFlow
            .drop(1)
            .sample(500)
            .flowOn(Dispatchers.IO)
            .onEach { (item, progress) ->
                if (progress != 0f) {
                    onProgressChanged(item, progress)
                }
            }.launchIn(lifecycleScope)
    }

    override fun onCreate() {
        Timber.d("onCreate")
        super.onCreate()

        setForeground()
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)

        Timber.d("onDestroy")
        super.onDestroy()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        lifecycleScope.launch {
            val item = intent?.taskItemOrNull ?: return@launch
            val userPreferences = userPreferencesRepository.data.first()
            val downloadPath = userPreferences.downloadPath
            val file = File(downloadPath, item.filename)

            if (file.exists()) {
                Timber.d("File already exists: ${file.absolutePath}")
                listeners[item]?.onFileExists()
                listeners[item]?.onSuccess()
                return@launch
            }

            val listener =
                object : IDownloadListener {
                    override fun getProgress(value: Float) {
                        listeners[item]?.getProgress(value)
                        progressFlow.value = item to value
                    }

                    override fun onSuccess() {
                        listeners[item]?.onSuccess()
                        progressFlow.value = item to 0f

                        onDownloadSucceeded(item)
                        tasks.remove(item)
                    }

                    override fun onFailure(e: Throwable) {
                        listeners[item]?.onFailure(e)
                        progressFlow.value = item to 0f

                        Timber.e(e)
                        onDownloadFailed(item, e.message)
                        tasks.remove(item)
                    }
                }

            val output =
                try {
                    val uri =
                        createDownloadUri(
                            path = file.toRelativeString(Const.PUBLIC_DOWNLOADS),
                            mimeType = "android/zip",
                        )
                    checkNotNull(contentResolver.openOutputStream(uri))
                } catch (e: Throwable) {
                    listener.onFailure(e)
                    return@launch
                }

            tasks.add(item)
            NetworkCompat
                .download(
                    url = item.url,
                    output = output,
                    onProgress = listener::getProgress,
                ).onSuccess {
                    listener.onSuccess()
                }.onFailure {
                    listener.onFailure(it)
                }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun onProgressChanged(
        item: TaskItem,
        progress: Float,
    ) {
        val notification =
            baseNotificationBuilder()
                .setContentTitle(item.title)
                .setSubText(item.desc)
                .setSilent(true)
                .setOngoing(true)
                .setGroup(GROUP_KEY)
                .setProgress(100, (progress * 100).toInt(), false)
                .build()

        notify(item.key, notification)
    }

    private fun onDownloadSucceeded(item: TaskItem) {
        val notification =
            baseNotificationBuilder()
                .setContentTitle(item.title)
                .setSubText(item.desc)
                .setContentText(getString(R.string.message_download_success))
                .setSilent(true)
                .build()

        notify(item.key, notification)
    }

    private fun onDownloadFailed(
        item: TaskItem,
        message: String?,
    ) {
        val notification =
            baseNotificationBuilder()
                .setContentTitle(item.title)
                .setSubText(item.desc)
                .setContentText(message ?: getString(R.string.unknown_error))
                .build()

        notify(item.key, notification)
    }

    private fun setForeground() {
        val notification =
            baseNotificationBuilder()
                .setContentTitle(getString(R.string.notification_name_download))
                .setSilent(true)
                .setOngoing(true)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .build()

        startForeground(NotificationUtils.NOTIFICATION_ID_DOWNLOAD, notification)
    }

    private fun baseNotificationBuilder() =
        NotificationCompat
            .Builder(this, NotificationUtils.CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(R.drawable.launcher_outline)

    @SuppressLint("MissingPermission")
    private fun notify(
        id: Int,
        notification: Notification,
    ) {
        val granted =
            if (BuildCompat.atLeastT) {
                PermissionCompat
                    .checkPermissions(
                        this,
                        listOf(Manifest.permission.POST_NOTIFICATIONS),
                    ).allGranted
            } else {
                true
            }

        NotificationManagerCompat.from(this).apply {
            if (granted) notify(id, notification)
        }
    }

    @Parcelize
    data class TaskItem(
        val key: Int,
        val url: String,
        val filename: String,
        val title: String?,
        val desc: String?,
    ) : Parcelable {
        companion object {
            fun empty() =
                TaskItem(
                    key = -1,
                    url = "",
                    filename = "",
                    title = null,
                    desc = null,
                )
        }
    }

    interface IDownloadListener {
        fun getProgress(value: Float) {}

        fun onFileExists() {}

        fun onSuccess() {}

        fun onFailure(e: Throwable) {}
    }

    companion object {
        private const val GROUP_KEY = "DOWNLOAD_SERVICE_GROUP_KEY"
        private const val EXTRA_TASK = "com.dergoogler.mmrl.extra.TASK"
        private val Intent.taskItemOrNull: TaskItem? get() = parcelable(EXTRA_TASK)

        private val listeners = hashMapOf<TaskItem, IDownloadListener>()
        private val progressFlow = MutableStateFlow(TaskItem.empty() to 0f)

        fun getProgressByKey(key: Int): Flow<Float> =
            progressFlow
                .filter { (item, _) ->
                    item.key == key
                }.map { (_, progress) ->
                    progress
                }

        fun start(
            context: Context,
            task: TaskItem,
            listener: IDownloadListener,
        ) {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT <= 29) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (BuildCompat.atLeastT) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            PermissionCompat.requestPermissions(context, permissions) { state ->
                if (state.allGranted) {
                    val intent = Intent(context, DownloadService::class.java)
                    intent.putExtra(EXTRA_TASK, task)

                    listeners[task] = listener
                    context.startService(intent)
                }
            }
        }
    }
}
