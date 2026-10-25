package com.dergoogler.mmrl.utils

import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
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
): T = createRootShell(globalMnt, debug, commands).use(block)

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
