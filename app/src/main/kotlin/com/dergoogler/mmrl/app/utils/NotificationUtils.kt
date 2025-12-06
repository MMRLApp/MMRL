package com.dergoogler.mmrl.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dergoogler.mmrl.R

object NotificationUtils {
    const val CHANNEL_ID_DOWNLOAD = "DOWNLOAD"
    const val CHANNEL_ID_PROVIDER = "PROVIDER"
    const val CHANNEL_ID_REPOSITORY = "REPOSITORY"
    const val CHANNEL_ID_MODULE = "MODULE"
    const val NOTIFICATION_ID_DOWNLOAD = 1024
    const val NOTIFICATION_ID_PROVIDER = 2024
    const val NOTIFICATION_ID_REPOSITORY = 3024
    const val NOTIFICATION_ID_MODULE = 4024

    fun init(context: Context) {
        val channels =
            listOf(
                NotificationChannel(
                    CHANNEL_ID_DOWNLOAD,
                    context.getString(R.string.notification_name_download),
                    NotificationManager.IMPORTANCE_HIGH,
                ),
                NotificationChannel(
                    CHANNEL_ID_REPOSITORY,
                    context.getString(R.string.notification_name_repository),
                    NotificationManager.IMPORTANCE_NONE,
                ),
                NotificationChannel(
                    CHANNEL_ID_PROVIDER,
                    context.getString(R.string.notification_name_provider),
                    NotificationManager.IMPORTANCE_NONE,
                ),
                NotificationChannel(
                    CHANNEL_ID_MODULE,
                    context.getString(R.string.notification_name_module),
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )

        NotificationManagerCompat.from(context).apply {
            createNotificationChannels(channels)
            deleteUnlistedNotificationChannels(channels.map { it.id })
        }
    }
}
