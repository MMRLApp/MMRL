package com.dergoogler.mmrl.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.App
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.ext.findActivity
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.modconf.helper.ModConfLauncher
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasModConf
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasWebUI
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.platform.model.toModuleConfig
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.webui.helper.WebUILauncher
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell

val Float.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

val Long.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }


@Throws(NoShellException::class)
inline fun <T> withNewRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
    block: Shell.() -> T,
): T {
    return createRootShell(globalMnt, debug, commands).use(block)
}

@Throws(NoShellException::class)
fun createRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
): Shell {
    Shell.enableVerboseLogging = debug
    val builder = Shell.Builder.create()
    if (globalMnt) {
        builder.setFlags(Shell.FLAG_MOUNT_MASTER)
    }
    return builder.build(*commands)
}

@SuppressLint("MissingPermission")
@Composable
fun UserPreferences.webUILauncher(context: Context, module: LocalModule): () -> Unit {
    val modId = module.id
    val config = modId.toModuleConfig()

    val activity = context.findActivity() as? ComponentActivity ?: run {
        Toast.makeText(context, "No activity found", Toast.LENGTH_SHORT).show()
        return {}
    }

    val modconfLauncher = ModConfLauncher(
        debug = BuildConfig.DEBUG,
        packageName = webuixPackageName
    )

    val webuiLauncher = WebUILauncher(
        debug = BuildConfig.DEBUG,
        packageName = webuixPackageName
    )

    fun Map<String, Boolean>.allGranted(onDenied: (String) -> Unit): Boolean {
        for ((perm, granted) in this) {
            if (!granted) {
                onDenied(perm)
                return false
            }
        }
        return true
    }

    val modconf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (!result.allGranted { perm ->
                Toast.makeText(context, "Permission denied! Requires $perm", Toast.LENGTH_SHORT)
                    .show()
            }
        ) return@rememberLauncherForActivityResult

        modconfLauncher.launch(
            context = context,
            modId = modId,
            platform = workingMode.toPlatform()
        )
    }

    val webui = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (!result.allGranted { perm ->
                Toast.makeText(context, "Permission denied! Requires $perm", Toast.LENGTH_SHORT)
                    .show()
            }
        ) return@rememberLauncherForActivityResult

        val effectiveEngine = when (webuiEngine) {
            WebUIEngine.PREFER_MODULE -> config.getWebuiEngine(context)?.let {
                when (it) {
                    "wx" -> WebUIEngine.WX
                    "ksu" -> WebUIEngine.KSU
                    else -> {
                        Toast.makeText(context, "Unknown WebUI engine", Toast.LENGTH_SHORT).show()
                        return@rememberLauncherForActivityResult
                    }
                }
            } ?: WebUIEngine.WX

            else -> webuiEngine
        }

        when (effectiveEngine) {
            WebUIEngine.WX -> webuiLauncher.launchWX(context, modId, workingMode.toPlatform())
            WebUIEngine.KSU -> webuiLauncher.launchLegacy(context, modId, workingMode.toPlatform())
            else -> Toast.makeText(context, "Unsupported WebUI engine", Toast.LENGTH_SHORT).show()
        }
    }

    return {
        when {
            module.hasModConf -> modconf.launch(arrayOf(modconfLauncher.permissions.MODCONF))
            module.hasWebUI -> webui.launch(
                arrayOf(
                    webuiLauncher.permissions.WEBUI_X,
                    webuiLauncher.permissions.WEBUI_LEGACY
                )
            )

            else -> Toast.makeText(context, "Unsupported module", Toast.LENGTH_SHORT).show()
        }
    }
}

val KsuNative.isManager: Boolean
    get() {
        val pkg = App.context.packageName
        return PlatformManager.platform.isKernelSuVariant && becomeManager(pkg)
    }