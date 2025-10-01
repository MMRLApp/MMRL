package com.dergoogler.mmrl.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

data class ScreenWidth(
    val isSmall: Boolean,
    val isMedium: Boolean,
    val isLarge: Boolean,
)

@Composable
fun currentScreenWidth(): ScreenWidth {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return ScreenWidth(
        isSmall = screenWidth < 600.dp, // Small screens are typically < 600dp
        isMedium = screenWidth >= 600.dp && screenWidth < 840.dp, // Medium screens are between 600dp and 840dp
        isLarge = screenWidth >= 840.dp // Large screens are 840dp or greater
    )
}