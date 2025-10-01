@file:Suppress("NOTHING_TO_INLINE")

package com.dergoogler.mmrl.ext

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

inline operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    OperatorPaddingValues(this, other, Dp::plus)

inline operator fun PaddingValues.minus(other: PaddingValues): PaddingValues =
    OperatorPaddingValues(this, other, Dp::minus)

@Immutable
class OperatorPaddingValues(
    private val that: PaddingValues,
    private val other: PaddingValues,
    private val operator: Dp.(Dp) -> Dp,
) : PaddingValues {
    override fun calculateBottomPadding(): Dp =
        operator(
            that.calculateBottomPadding(),
            other.calculateBottomPadding()
        )

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        operator(
            that.calculateLeftPadding(layoutDirection),
            other.calculateLeftPadding(layoutDirection)
        )

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        operator(
            that.calculateRightPadding(layoutDirection),
            other.calculateRightPadding(layoutDirection)
        )

    override fun calculateTopPadding(): Dp =
        operator(
            that.calculateTopPadding(),
            other.calculateTopPadding()
        )
}

inline fun PaddingValues.top() = PaddingValues(top = calculateTopPadding())

inline fun PaddingValues.bottom() = PaddingValues(bottom = calculateBottomPadding())

inline fun PaddingValues.vertical() = PaddingValues(
    top = calculateTopPadding(),
    bottom = calculateBottomPadding()
)

@Composable
inline fun PaddingValues.start() = with(LocalLayoutDirection.current) {
    PaddingValues(start = calculateStartPadding(this))
}

@Composable
inline fun PaddingValues.end() = with(LocalLayoutDirection.current) {
    PaddingValues(end = calculateEndPadding(this))
}

@Composable
inline fun PaddingValues.horizontal() = with(LocalLayoutDirection.current) {
    PaddingValues(
        start = calculateStartPadding(this),
        end = calculateEndPadding(this)
    )
}

@Composable
inline fun PaddingValues.copy(
    start: Dp = calculateTopPadding(),
    top: Dp = with(LocalLayoutDirection.current) { calculateStartPadding(this) },
    end: Dp = with(LocalLayoutDirection.current) { calculateEndPadding(this) },
    bottom: Dp = calculateBottomPadding()
) = PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom
)