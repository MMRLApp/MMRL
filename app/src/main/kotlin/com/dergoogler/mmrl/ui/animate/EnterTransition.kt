package com.dergoogler.mmrl.ui.animate

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.ui.unit.IntOffset

fun slideInBottomToTop(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideIn(
    initialOffset = { IntOffset(0, it.height) },
    animationSpec = animationSpec,
)

fun slideInTopToBottom(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideIn(
    initialOffset = { IntOffset(0, -it.height) },
    animationSpec = animationSpec,
)

fun slideInLeftToRight(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideIn(
    initialOffset = { IntOffset(-it.width, 0) },
    animationSpec = animationSpec,
)

fun slideInRightToLeft(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
) = slideIn(
    initialOffset = { IntOffset(it.width, 0) },
    animationSpec = animationSpec,
)
