package com.dergoogler.mmrl.ext

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

data class ScreenWidth(
    val isSmall: Boolean,
    val isMedium: Boolean,
    val isLarge: Boolean,
)

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
        // Use window metrics for more accurate measurement
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val widthPixels = windowMetrics.bounds.width()
        with(density) { widthPixels.toDp() }
    } else {
        // Fallback to configuration-based width
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
