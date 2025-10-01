package com.dergoogler.mmrl.ui.component.toolbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.BlurUtil
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BlurBottomToolbar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    val prefs = LocalUserPreferences.current

    val isBlurEnabled = remember(prefs) {
        prefs.enableBlur && BlurUtil.isBlurSupported()
    }

    val blurModifier = if (isBlurEnabled) {
        Modifier.hazeEffect(
            state = LocalHazeState.current,
            style = HazeMaterials.ultraThin()
        ) {
            backgroundColor = containerColor
        }
    } else Modifier

    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Column {
        NavigationBar(
            modifier = Modifier
                .then(blurModifier)
                .drawBehind {
                    val borderSize = Dp.Hairline
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = borderSize.value
                    )
                }
                .then(modifier),
            containerColor = if (isBlurEnabled) Color.Transparent else containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            windowInsets = windowInsets,
            content = content
        )
    }
}
