package com.dergoogler.mmrl.ui.component.toolbar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
fun BlurToolbar(
    title: @Composable (Float) -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    fadeBackgroundIfNoBlur: Boolean = false,
    fadeDistance: Float = 200f,
    fade: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val prefs = LocalUserPreferences.current
    val state = scrollBehavior?.state

    val isBlurEnabled = remember(prefs) {
        prefs.enableBlur && BlurUtil.isBlurSupported()
    }

    val targetAlpha by remember {
        derivedStateOf {
            if (!fade || state == null) {
                1f
            } else {
                ((-state.contentOffset) / fadeDistance).coerceIn(0f, 1f)
            }
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(
            durationMillis = 10,
            easing = FastOutSlowInEasing
        ),
        label = "toolbar_alpha"
    )

    val borderColor = MaterialTheme.colorScheme.outline
    val backgroundColor = MaterialTheme.colorScheme.background

    val topAppBarColors = TopAppBarDefaults.topAppBarColors()

    val colors = if (isBlurEnabled) {
        topAppBarColors.copy(
            scrolledContainerColor = Color.Transparent,
            containerColor = Color.Transparent
        )
    } else {
        val backgroundColor = MaterialTheme.colorScheme.background
            .let {
                if (fadeBackgroundIfNoBlur) {
                    it.copy(alpha = animatedAlpha)
                } else it
            }

        topAppBarColors.copy(
            scrolledContainerColor = backgroundColor,
            containerColor = backgroundColor
        )
    }

    val blur = if (isBlurEnabled && animatedAlpha > 0.01f) {
        Modifier.hazeEffect(
            state = LocalHazeState.current,
            style = HazeMaterials.ultraThin()
        ) {
            this@hazeEffect.backgroundColor = backgroundColor
            this@hazeEffect.alpha = animatedAlpha
        }
    } else Modifier

    TopAppBar(
        title = { title(animatedAlpha) },
        modifier = Modifier
            .drawBehind {
                if (animatedAlpha > 0.01f) {
                    val borderSize = Dp.Hairline
                    val y = size.height - borderSize.value
                    drawLine(
                        color = borderColor.copy(alpha = animatedAlpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = borderSize.value
                    )
                }
            }
            .then(blur)
            .then(modifier),
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        expandedHeight = expandedHeight,
    )
}