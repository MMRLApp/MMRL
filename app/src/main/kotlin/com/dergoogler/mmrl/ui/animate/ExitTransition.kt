package com.dergoogler.mmrl.ui.animate

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset

fun slideOutTopToBottom(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideOut(
    targetOffset = { IntOffset(0, it.height) },
    animationSpec = animationSpec,
)

fun slideOutBottomToTop(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideOut(
    targetOffset = { IntOffset(0, -it.height) },
    animationSpec = animationSpec,
)

fun slideOutRightToLeft(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideOut(
    targetOffset = { IntOffset(-it.width, 0) },
    animationSpec = animationSpec,
)

fun slideOutLeftToRight(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideOut(
    targetOffset = { IntOffset(it.width, 0) },
    animationSpec = animationSpec,
)
