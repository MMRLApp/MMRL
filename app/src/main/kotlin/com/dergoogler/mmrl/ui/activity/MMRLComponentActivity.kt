package com.dergoogler.mmrl.ui.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.compat.PermissionCompat
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.ext.compose.providable.LocalActivity
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalLifecycle
import com.dergoogler.mmrl.ui.providable.LocalLifecycleScope
import com.dergoogler.mmrl.ui.providable.LocalMainNavController
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalSuperUserViewModel
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.theme.Colors
import com.dergoogler.mmrl.ui.theme.MMRLAppTheme
import com.dergoogler.mmrl.viewmodel.SettingsViewModel
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import dagger.hilt.android.AndroidEntryPoint
import dev.dergoogler.mmrl.compat.BuildCompat
import dev.dergoogler.mmrl.compat.core.BrickException
import dev.dergoogler.mmrl.compat.core.MMRLUriHandlerImpl
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
open class MMRLComponentActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var localRepository: LocalRepository

    @Inject
    lateinit var modulesRepository: ModulesRepository

    open val requirePermissions = listOf<String>()
    var permissionsGranted = true

    /**
     * The window flags to apply to the activity window. These flags will be cleared once the activity is destroyed.
     */
    open val windowFlags: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            throwable.printStackTrace()
            startCrashActivity(thread, throwable)
        }

        if (windowFlags != 0) {
            Timber.d("Setting window flags")
            this.window.addFlags(windowFlags)
        }

        val granted = if (BuildCompat.atLeastT) {
            PermissionCompat.checkPermissions(
                this,
                requirePermissions
            ).allGranted
        } else {
            true
        }

        if (!granted) {
            PermissionCompat.requestPermissions(this, requirePermissions) { state ->
                permissionsGranted = state.allGranted
            }
        }
    }

    private fun startCrashActivity(thread: Thread, throwable: Throwable) {
        val intent = Intent(this, CrashHandlerActivity::class.java).apply {
            putExtra("message", throwable.message)
            if (throwable is BrickException) {
                putExtra("helpMessage", throwable.helpMessage)
            }
            putExtra("stacktrace", formatStackTrace(throwable))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        exitProcess(0)
    }

    private fun formatStackTrace(throwable: Throwable, numberOfLines: Int = 88): String {
        val stackTrace = throwable.stackTrace
        val stackTraceElements = stackTrace.joinToString("\n") { it.toString() }

        return if (stackTrace.size > numberOfLines) {
            val trimmedStackTrace =
                stackTraceElements.lines().take(numberOfLines).joinToString("\n")
            val moreCount = stackTrace.size - numberOfLines

            getString(R.string.stack_trace_truncated, trimmedStackTrace, moreCount)
        } else {
            stackTraceElements
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (windowFlags != 0) {
            Timber.d("Clearing window flags")
            this.window.clearFlags(windowFlags)
        }
    }

    fun createNotificationChannel(
        channelName: String,
        @StringRes channelTitle: Int,
        @StringRes channelDesc: Int,
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelName,
            applicationContext.getString(channelTitle),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.description = applicationContext.getString(channelDesc)

        notificationManager.createNotificationChannel(channel)
    }

    inline fun <reified A : ComponentActivity> setActivityEnabled(enable: Boolean) {
        val component = ComponentName(
            this, A::class.java
        )

        val state = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}

@Composable
fun BaseContent(
    activity: ComponentActivity,
    userPreferencesRepository: UserPreferencesRepository,
    content: @Composable () -> Unit,
) {
    val userPreferences by userPreferencesRepository.data.collectAsStateWithLifecycle(
        initialValue = null
    )

    val navController = rememberNavController()
    val mainNavController = rememberNavController()
    val navigator = navController.rememberDestinationsNavigator()

    val preferences = if (userPreferences == null) {
        return
    } else {
        checkNotNull(userPreferences)
    }

    val context = LocalContext.current
    val currentTheme = Colors.getColor(preferences.themeColor, preferences.isDarkMode())
    val toolbarColor = currentTheme.surface

    MMRLAppTheme(
        darkMode = preferences.isDarkMode(),
        navController = navController,
        themeColor = preferences.themeColor,
        providerValues = arrayOf(
            LocalDestinationsNavigator provides navigator,
            LocalActivity provides activity,
            LocalSuperUserViewModel provides hiltViewModel<SuperUserViewModel>(activity),
            LocalSettings provides hiltViewModel<SettingsViewModel>(activity),
            LocalUserPreferences provides preferences,
            dev.dergoogler.mmrl.compat.core.LocalUriHandler provides MMRLUriHandlerImpl(
                context,
                toolbarColor
            ),
            LocalLifecycleScope provides activity.lifecycleScope,
            LocalLifecycle provides activity.lifecycle,
            LocalUriHandler provides MMRLUriHandlerImpl(context, toolbarColor),
            LocalNavController provides navController,
            LocalMainNavController provides mainNavController
        ),
        content = content
    )
}

fun MMRLComponentActivity.setBaseContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
) = this.setContent(
    parent = parent,
) {
    BaseContent(
        this,
        userPreferencesRepository,
        content
    )
}