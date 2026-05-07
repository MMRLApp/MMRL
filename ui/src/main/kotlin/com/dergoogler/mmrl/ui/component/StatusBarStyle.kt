package com.dergoogler.mmrl.ui.component

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

/**
 * Configures the status bar and navigation bar styles for the application.
 *
 * This composable allows you to customize the appearance of the status bar and navigation bar,
 * including whether they are light or dark, and the color of their scrim (background overlay).
 * It uses the `enableEdgeToEdge` function from the AndroidX Activity library to achieve
 * a modern, immersive look where the app content can extend under the system bars.
 *
 * @param darkMode A boolean indicating whether to use a dark theme (true) or light theme (false).
 *                 Defaults to the system's dark theme setting.
 * @param statusBarScrim The color to use for the status bar's background scrim.
 *                       Defaults to transparent.
 * @param navigationBarScrim The color to use for the navigation bar's background scrim.
 *                           Defaults to transparent.
 *
 * Usage:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     StatusBarStyle(darkMode = true, statusBarScrim = Color.Black.copy(alpha = 0.2f), navigationBarScrim = Color.Black.copy(alpha = 0.5f))
 *     // ... rest of your composable content ...
 * }
 * ```
 *
 * In this example, the status bar will be slightly transparent dark, and the navigation bar will be semi-transparent dark.
 *
 * Note: This function should typically be called at the top level of your Composable hierarchy or within a screen's composable,
 * to ensure the system bars are styled correctly throughout the app's content.
 */
@Composable
fun StatusBarStyle(
    darkMode: Boolean = isSystemInDarkTheme(),
    statusBarScrim: Color = Color.Transparent,
    navigationBarScrim: Color = Color.Transparent,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle =
                androidx.activity.SystemBarStyle.auto(
                    statusBarScrim.toArgb(),
                    statusBarScrim.toArgb(),
                ) { darkMode },
            navigationBarStyle =
                when {
                    darkMode ->
                        androidx.activity.SystemBarStyle.dark(
                            navigationBarScrim.toArgb(),
                        )
                    else ->
                        androidx.activity.SystemBarStyle.light(
                            navigationBarScrim.toArgb(),
                            navigationBarScrim.toArgb(),
                        )
                },
        )
    }
}
