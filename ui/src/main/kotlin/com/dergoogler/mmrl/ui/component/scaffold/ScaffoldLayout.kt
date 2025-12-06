package com.dergoogler.mmrl.ui.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapNotNull
import androidx.compose.ui.util.fastMaxBy

@Composable
fun ScaffoldLayout(
    fabPosition: FabPosition,
    topBar: @Composable () -> Unit,
    snackbar: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    bottomBar: @Composable () -> Unit,
    railBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val layoutDirection = this.layoutDirection

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val railBarPlaceables =
            subcompose(ScaffoldLayoutContent.NavigationRail, railBar).fastMap {
                it.measure(looseConstraints)
            }
        val railBarWidth = railBarPlaceables.fastMaxBy { it.width }?.width ?: 0
        val hasRail = railBarWidth > 0

        val topBarPlaceables =
            subcompose(ScaffoldLayoutContent.TopBar, topBar).fastMap {
                it.measure(looseConstraints)
            }

        val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0

        val snackbarPlaceables =
            subcompose(ScaffoldLayoutContent.Snackbar, snackbar).fastMap {
                val leftInset = contentWindowInsets.getLeft(this, layoutDirection)
                val rightInset = contentWindowInsets.getRight(this, layoutDirection)
                val bottomInset = contentWindowInsets.getBottom(this)
                it.measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))
            }
        val snackbarHeight = snackbarPlaceables.fastMaxBy { it.height }?.height ?: 0
        val snackbarWidth = snackbarPlaceables.fastMaxBy { it.width }?.width ?: 0

        val fabPlaceables =
            subcompose(ScaffoldLayoutContent.Fab, fab).fastMapNotNull { measurable ->
                val leftInset = contentWindowInsets.getLeft(this, layoutDirection)
                val rightInset = contentWindowInsets.getRight(this, layoutDirection)
                val bottomInset = contentWindowInsets.getBottom(this)
                measurable
                    .measure(looseConstraints.offset(-leftInset - rightInset, -bottomInset))
                    .takeIf { it.height != 0 && it.width != 0 }
            }

        val fabPlacement =
            if (fabPlaceables.isNotEmpty()) {
                val fabWidth = fabPlaceables.fastMaxBy { it.width }!!.width
                val fabHeight = fabPlaceables.fastMaxBy { it.height }!!.height
                val fabLeftOffset =
                    when (fabPosition) {
                        FabPosition.Start -> {
                            if (layoutDirection == LayoutDirection.Ltr) {
                                FabSpacing.roundToPx() + if (hasRail) railBarWidth else 0
                            } else {
                                layoutWidth - FabSpacing.roundToPx() - fabWidth - if (hasRail) railBarWidth else 0
                            }
                        }
                        FabPosition.End, FabPosition.EndOverlay -> {
                            if (layoutDirection == LayoutDirection.Ltr) {
                                layoutWidth - FabSpacing.roundToPx() - fabWidth
                            } else {
                                FabSpacing.roundToPx() + if (hasRail) railBarWidth else 0
                            }
                        }
                        else -> (layoutWidth - fabWidth) / 2
                    }
                FabPlacement(left = fabLeftOffset, width = fabWidth, height = fabHeight)
            } else {
                null
            }

        val bottomBarPlaceables =
            subcompose(ScaffoldLayoutContent.BottomBar, bottomBar).fastMap {
                it.measure(looseConstraints.offset(if (hasRail) -railBarWidth else 0))
            }

        val bottomBarHeight = bottomBarPlaceables.fastMaxBy { it.height }?.height

        val fabOffsetFromBottom =
            fabPlacement?.let {
                if (bottomBarHeight == null || fabPosition == FabPosition.EndOverlay) {
                    it.height + FabSpacing.roundToPx() + contentWindowInsets.getBottom(this)
                } else {
                    bottomBarHeight + it.height + FabSpacing.roundToPx()
                }
            }

        val snackbarOffsetFromBottom =
            if (snackbarHeight != 0) {
                snackbarHeight + (fabOffsetFromBottom ?: bottomBarHeight ?: contentWindowInsets.getBottom(this))
            } else {
                0
            }

        val bodyContentPlaceables =
            subcompose(ScaffoldLayoutContent.MainContent) {
                val insets = contentWindowInsets.asPaddingValues(this)

                val innerPadding =
                    PaddingValues(
                        top =
                            if (topBarPlaceables.isEmpty()) {
                                insets.calculateTopPadding()
                            } else {
                                topBarHeight.toDp()
                            },
                        bottom =
                            if (bottomBarPlaceables.isEmpty() || bottomBarHeight == null) {
                                insets.calculateBottomPadding()
                            } else {
                                bottomBarHeight.toDp()
                            },
                        start =
                            if (hasRail) {
                                railBarWidth.toDp()
                            } else {
                                insets.calculateStartPadding(layoutDirection)
                            },
                        end = insets.calculateEndPadding(layoutDirection),
                    )

                content(innerPadding)
            }.fastMap { it.measure(looseConstraints) }

        layout(layoutWidth, layoutHeight) {
            railBarPlaceables.fastForEach { it.place(0, 0) }
            bodyContentPlaceables.fastForEach { it.place(0, 0) }
            topBarPlaceables.fastForEach { it.place(0, 0) }

            snackbarPlaceables.fastForEach {
                it.place(
                    (layoutWidth - snackbarWidth) / 2 + contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection),
                    layoutHeight - snackbarOffsetFromBottom,
                )
            }

            bottomBarPlaceables.fastForEach {
                it.place(
                    if (hasRail) railBarWidth else 0,
                    layoutHeight - (bottomBarHeight ?: 0),
                )
            }

            fabPlacement?.let { placement ->
                fabPlaceables.fastForEach {
                    it.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
                }
            }
        }
    }
}
