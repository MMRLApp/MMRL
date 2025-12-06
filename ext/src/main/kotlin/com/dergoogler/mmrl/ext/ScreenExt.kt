package com.dergoogler.mmrl.ext

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

data class ScreenWidth(
    val isSmall: Boolean,
    val isMedium: Boolean,
    val isLarge: Boolean,
)

/**
 * Calculates the current screen width classification using Material 3 window size class breakpoints.
 * 
 * This function uses WindowMetrics API (API 30+) or display metrics to get accurate screen width
 * measurements that are not affected by custom DPI settings. This is more reliable than using
 * configuration.screenWidthDp directly, especially on devices with high DPI settings.
 * 
 * Note: This function requires an Activity context for accurate measurements. In non-Activity
 * contexts (e.g., Compose Previews), it falls back to configuration.screenWidthDp which may
 * be less accurate with custom DPI settings.
 * 
 * @return ScreenWidth data class with boolean flags for small/medium/large classifications
 */
@Composable
fun currentScreenWidth(): ScreenWidth {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // Get the actual window width in pixels, then convert to dp using density
    // This approach is more reliable than using configuration.screenWidthDp directly
    // especially with high DPI settings
    val activity = context as? Activity
    val screenWidthDp = if (activity != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Use window metrics for more accurate measurement (API 30+)
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val widthPixels = windowMetrics.bounds.width()
            with(density) { widthPixels.toDp() }
        } else {
            // Fallback for API 26-29: use display metrics
            // Remember the DisplayMetrics instance to avoid recreating on recomposition
            val displayMetrics = remember { DisplayMetrics() }
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            with(density) { displayMetrics.widthPixels.toDp() }
        }
    } else {
        // Fallback to configuration-based width when not in an Activity context
        // This occurs in Compose Previews or other non-Activity contexts
        // Note: This may be less accurate with custom DPI settings
        configuration.screenWidthDp.dp
    }
    
    // Use Material 3 window size class breakpoints
    // Compact: width < 600dp
    // Medium: 600dp <= width < 840dp  
    // Expanded: width >= 840dp
    return ScreenWidth(
        isSmall = screenWidthDp < 600.dp,
        isMedium = screenWidthDp >= 600.dp && screenWidthDp < 840.dp,
        isLarge = screenWidthDp >= 840.dp,
    )
}
