package com.dergoogler.mmrl.ui.component.toolbar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.BlurUtil
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.mmrlx.compose.ui.theme.MMRLXTheme
import dev.mmrlx.compose.ui.theme.Oklch
import dev.mmrlx.compose.ui.toolbar.ToolbarDefaults
import dev.mmrlx.compose.ui.toolbar.ToolbarScrollBehavior

private const val VISIBILITY_THRESHOLD = 0.01f

/**
 * Read-only view of the toolbar's current visual state.
 * All properties are snapshot-backed, so reading them in composition
 * (or in a draw/layout lambda) will trigger the right invalidation.
 */
@Stable
class BlurToolbarState internal constructor(
    private val animatedAlpha: State<Float>,
    private val targetAlphaState: State<Float>,
    private val blurEnabled: State<Boolean>,
    private val scrollBehavior: ToolbarScrollBehavior?,
) {
    /** Current (animated) alpha, 0f..1f. */
    val alpha: Float get() = animatedAlpha.value

    /** Un-animated alpha derived from scroll position. */
    val targetAlpha: Float get() = targetAlphaState.value

    /** Blur is enabled in prefs AND supported on this device. */
    val isBlurEnabled: Boolean get() = blurEnabled.value

    /** True if the toolbar is visible enough to draw anything. */
    val isVisible: Boolean get() = alpha > VISIBILITY_THRESHOLD

    /** True if the haze blur effect is actually being applied. */
    val isBlurActive: Boolean get() = isBlurEnabled && isVisible

    val isFullyVisible: Boolean get() = alpha >= 1f - VISIBILITY_THRESHOLD

    /** True while the alpha animation is still catching up with its target. */
    val isAnimating: Boolean get() = alpha != targetAlpha

    /** Raw scroll content offset (<= 0 when scrolled), or 0f without a scroll behavior. */
    val contentOffset: Float get() = scrollBehavior?.state?.contentOffset ?: 0f

    /** 0f = expanded, 1f = fully collapsed. */
    val collapsedFraction: Float get() = scrollBehavior?.state?.collapsedFraction ?: 0f

    /** 0f..1f, how much content overlaps the toolbar. */
    val overlappedFraction: Float get() = scrollBehavior?.state?.overlappedFraction ?: 0f
}

@Composable
fun rememberBlurToolbarState(
    scrollBehavior: ToolbarScrollBehavior? = null,
    fade: Boolean = false,
    fadeDistance: Float = 200f,
): BlurToolbarState {
    val prefs = LocalUserPreferences.current
    val scrollState = scrollBehavior?.state

    val blurEnabled = remember(prefs) {
        derivedStateOf { prefs.enableBlur && BlurUtil.isBlurSupported() }
    }

    val targetAlpha = remember(scrollState, fade, fadeDistance) {
        derivedStateOf {
            if (!fade || scrollState == null || fadeDistance <= 0f) {
                1f
            } else {
                (-scrollState.contentOffset / fadeDistance).coerceIn(0f, 1f)
            }
        }
    }

    val animatedAlpha = animateFloatAsState(
        targetValue = targetAlpha.value,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "toolbar_alpha",
    )

    return remember(scrollBehavior, blurEnabled, targetAlpha, animatedAlpha) {
        BlurToolbarState(
            animatedAlpha = animatedAlpha,
            targetAlphaState = targetAlpha,
            blurEnabled = blurEnabled,
            scrollBehavior = scrollBehavior,
        )
    }
}

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
    scrollBehavior: ToolbarScrollBehavior? = null,
    state: BlurToolbarState = rememberBlurToolbarState(
        scrollBehavior = scrollBehavior,
        fade = fade,
        fadeDistance = fadeDistance,
    ),
) {
    val borderColor = MMRLXTheme.border.okLchColor
    val backgroundColor = MMRLXTheme.oklchColors.background
    val topAppBarColors = ToolbarDefaults.colors()

    val colors = if (state.isBlurEnabled) {
        topAppBarColors.copy(
            scrolledContainerColor = Oklch(0.0,0.0, 0.0,0.0),
            containerColor = Oklch(0.0,0.0, 0.0,0.0),
        )
    } else {
        val container = if (fadeBackgroundIfNoBlur) {
            backgroundColor.copy(opacity = state.alpha.toDouble())
        } else {
            backgroundColor
        }
        topAppBarColors.copy(
            scrolledContainerColor = container,
            containerColor = container,
        )
    }

    dev.mmrlx.compose.ui.toolbar.Toolbar(
        title = { title(state.alpha) },
        modifier = Modifier
            .drawBehind {
                // Read inside the draw scope so alpha changes only redraw
                val alpha = state.alpha
                if (alpha > VISIBILITY_THRESHOLD) {
                    val borderSize = Dp.Hairline
                    val y = size.height - borderSize.value
                    drawLine(
                        color = borderColor.copy(opacity = alpha.toDouble()).toColor(),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = borderSize.value,
                    )
                }
            }
            .then(modifier),
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
    )
}