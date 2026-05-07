package com.dergoogler.mmrl.ui.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopAppBar(
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = CollapsingTopAppBarDefaults.windowInsets,
    colors: CollapsingTopAppBarColors = CollapsingTopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CollapsingTopAppBarLayout(
        title = {
            Column(
                modifier = Modifier.padding(end = TopAppBarTitleInset),
                content = content,
            )
        },
        titleTextStyle = MaterialTheme.typography.titleLarge,
        smallTitleTextStyle = MaterialTheme.typography.titleLarge,
        smallTitle = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = windowInsets,
        pinnedHeight = 64.0.dp,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingTopAppBarLayout(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    smallTitle: @Composable () -> Unit,
    smallTitleTextStyle: TextStyle,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    colors: CollapsingTopAppBarColors,
    pinnedHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    var maxHeightPx by remember { mutableFloatStateOf(0f) }
    val pinnedHeightPx =
        with(LocalDensity.current) {
            pinnedHeight.toPx()
        }

    // Sets the app bar's height offset limit to hide just the bottom title area and keep top title
    // visible when collapsed.
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != pinnedHeightPx - maxHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = pinnedHeightPx - maxHeightPx
        }
    }

    // Obtain the container Color from the TopAppBarColors using the `collapsedFraction`, as the
    // bottom part of this TwoRowsTopAppBar changes color at the same rate the app bar expands or
    // collapse.
    // This will potentially animate or interpolate a transition between the container color and the
    // container's scrolled color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val appBarContainerColor = colors.containerColor(colorTransitionFraction)

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
    val topTitleAlpha = TopTitleAlphaEasing.transform(colorTransitionFraction)
    val bottomTitleAlpha = 1f - colorTransitionFraction
    // Hide the top row title semantics when its alpha value goes below 0.5 threshold.
    // Hide the bottom row title semantics when the top title semantics are active.
    val hideTopRowSemantics = colorTransitionFraction < 0.5f
    val hideBottomRowSemantics = !hideTopRowSemantics

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier =
        if (scrollBehavior != null && !scrollBehavior.isPinned) {
            Modifier.draggable(
                orientation = Orientation.Vertical,
                state =
                    rememberDraggableState { delta ->
                        scrollBehavior.state.heightOffset += delta
                    },
                onDragStopped = { velocity ->
                    settleAppBar(
                        scrollBehavior.state,
                        velocity,
                        scrollBehavior.flingAnimationSpec,
                        scrollBehavior.snapAnimationSpec,
                    )
                },
            )
        } else {
            Modifier
        }

    Surface(
        modifier = modifier.then(appBarDragModifier),
        color = appBarContainerColor,
    ) {
        Column {
            TopAppBarLayout(
                modifier =
                    Modifier
                        .windowInsetsPadding(windowInsets)
                        // clip after padding so we don't show the title over the inset area
                        .clipToBounds(),
                heightPx = pinnedHeightPx,
                navigationIconContentColor =
                    colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor =
                    colors.actionIconContentColor,
                title = smallTitle,
                titleTextStyle = smallTitleTextStyle,
                titleAlpha = topTitleAlpha,
                titleVerticalArrangement = Arrangement.Center,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = 0,
                hideTitleSemantics = hideTopRowSemantics,
                navigationIcon = navigationIcon,
                actions = actionsRow,
            )

            Layout(
                content = {
                    Box(
                        Modifier
                            .layoutId("title")
                            .then(
                                if (hideBottomRowSemantics) {
                                    Modifier.clearAndSetSemantics { }
                                } else {
                                    Modifier
                                },
                            ).graphicsLayer(alpha = bottomTitleAlpha),
                    ) {
                        ProvideContentColorTextStyle(
                            contentColor = colors.titleContentColor,
                            textStyle = titleTextStyle,
                            content = title,
                        )
                    }
                },
                modifier =
                    Modifier
                        // only apply the horizontal sides of the window insets padding, since the top
                        // padding will always be applied by the layout above
                        .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
                        .clipToBounds(),
            ) { measurables, constraints ->
                val titlePlaceable =
                    measurables
                        .fastFirst { it.layoutId == "title" }
                        .measure(constraints.copy(minWidth = 0))
                        .apply {
                            maxHeightPx = pinnedHeightPx + height
                        }

                val heightPx =
                    maxHeightPx - pinnedHeightPx + (
                        scrollBehavior?.state?.heightOffset
                            ?: 0f
                    )

                val layoutHeight = if (heightPx.isNaN()) 0 else heightPx.roundToInt()
                layout(constraints.maxWidth, layoutHeight) {
                    titlePlaceable.placeRelative(
                        x = 0,
                        y = 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppBarLayout(
    modifier: Modifier,
    heightPx: Float,
    navigationIconContentColor: Color,
    titleContentColor: Color,
    actionIconContentColor: Color,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    titleAlpha: Float,
    titleVerticalArrangement: Arrangement.Vertical,
    titleHorizontalArrangement: Arrangement.Horizontal,
    titleBottomPadding: Int,
    hideTitleSemantics: Boolean,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    Layout(
        content = {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .padding(start = TopAppBarHorizontalPadding),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides navigationIconContentColor,
                    content = navigationIcon,
                )
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = TopAppBarHorizontalPadding)
                    .then(if (hideTitleSemantics) Modifier.clearAndSetSemantics { } else Modifier)
                    .graphicsLayer(alpha = titleAlpha),
            ) {
                ProvideContentColorTextStyle(
                    contentColor = titleContentColor,
                    textStyle = titleTextStyle,
                    content = title,
                )
            }
            Box(
                Modifier
                    .layoutId("actionIcons")
                    .padding(end = TopAppBarHorizontalPadding),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides actionIconContentColor,
                    content = actions,
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0))
        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0))

        val maxTitleWidth =
            if (constraints.maxWidth == Constraints.Infinity) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width)
                    .coerceAtLeast(0)
            }
        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = maxTitleWidth))

        // Locate the title's baseline.
        val titleBaseline =
            if (titlePlaceable[LastBaseline] != AlignmentLine.Unspecified) {
                titlePlaceable[LastBaseline]
            } else {
                0
            }

        val layoutHeight = if (heightPx.isNaN()) 0 else heightPx.roundToInt()

        layout(constraints.maxWidth, layoutHeight) {
            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = (layoutHeight - navigationIconPlaceable.height) / 2,
            )

            // Title
            titlePlaceable.placeRelative(
                x =
                    when (titleHorizontalArrangement) {
                        Arrangement.Center -> {
                            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
                            if (baseX < navigationIconPlaceable.width) {
                                // May happen if the navigation is wider than the actions and the
                                // title is long. In this case, prioritize showing more of the title by
                                // offsetting it to the right.
                                baseX += (navigationIconPlaceable.width - baseX)
                            } else if (baseX + titlePlaceable.width >
                                constraints.maxWidth - actionIconsPlaceable.width
                            ) {
                                // May happen if the actions are wider than the navigation and the title
                                // is long. In this case, offset to the left.
                                baseX += (
                                    (constraints.maxWidth - actionIconsPlaceable.width) -
                                        (baseX + titlePlaceable.width)
                                )
                            }
                            baseX
                        }

                        Arrangement.End ->
                            constraints.maxWidth - titlePlaceable.width - actionIconsPlaceable.width
                        // Arrangement.Start.
                        // An TopAppBarTitleInset will make sure the title is offset in case the
                        // navigation icon is missing.
                        else -> max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                    },
                y =
                    when (titleVerticalArrangement) {
                        Arrangement.Center -> (layoutHeight - titlePlaceable.height) / 2
                        // Apply bottom padding from the title's baseline only when the Arrangement is
                        // "Bottom".
                        Arrangement.Bottom ->
                            if (titleBottomPadding == 0) {
                                layoutHeight - titlePlaceable.height
                            } else {
                                layoutHeight - titlePlaceable.height -
                                    max(
                                        0,
                                        titleBottomPadding - titlePlaceable.height + titleBaseline,
                                    )
                            }
                        // Arrangement.Top
                        else -> 0
                    },
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = (layoutHeight - actionIconsPlaceable.height) / 2,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        ).animateDecay(flingAnimationSpec) {
            val delta = value - lastValue
            val initialHeightOffset = state.heightOffset
            state.heightOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - state.heightOffset)
            lastValue = value
            remainingVelocity = this.velocity
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec,
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}

// An easing function used to compute the alpha value that is applied to the top title part of a
// Medium or Large app bar.
// @VisibleForTesting
internal val TopTitleAlphaEasing = CubicBezierEasing(.8f, 0f, .8f, .15f)

private val TopAppBarHorizontalPadding = 4.dp

// A title inset when the App-Bar is a Medium or Large one. Also used to size a spacer when the
// navigation icon is missing.
private val TopAppBarTitleInset = 16.dp - TopAppBarHorizontalPadding

@Immutable
class CollapsingTopAppBarColors internal constructor(
    private val containerColor: Color,
    private val scrolledContainerColor: Color,
    internal val navigationIconContentColor: Color,
    internal val titleContentColor: Color,
    internal val actionIconContentColor: Color,
) {
    @Composable
    internal fun containerColor(colorTransitionFraction: Float): Color =
        lerp(
            containerColor,
            scrolledContainerColor,
            FastOutLinearInEasing.transform(colorTransitionFraction),
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is CollapsingTopAppBarColors) return false

        if (containerColor != other.containerColor) return false
        if (scrolledContainerColor != other.scrolledContainerColor) return false
        if (navigationIconContentColor != other.navigationIconContentColor) return false
        if (titleContentColor != other.titleContentColor) return false
        if (actionIconContentColor != other.actionIconContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + scrolledContainerColor.hashCode()
        result = 31 * result + navigationIconContentColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + actionIconContentColor.hashCode()

        return result
    }
}

object CollapsingTopAppBarDefaults {
    @Composable
    fun topAppBarColors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        scrolledContainerColor: Color =
            MaterialTheme.colorScheme.applyTonalElevation(
                backgroundColor = containerColor,
                elevation = 3.0.dp,
            ),
        navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
        titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ) = CollapsingTopAppBarColors(
        containerColor,
        scrolledContainerColor,
        navigationIconContentColor,
        titleContentColor,
        actionIconContentColor,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun scrollBehavior(
        state: TopAppBarState = rememberTopAppBarState(),
        canScroll: () -> Boolean = { true },
        snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
    ) = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state,
        canScroll,
        snapAnimationSpec,
        flingAnimationSpec,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    val windowInsets @Composable get() = TopAppBarDefaults.windowInsets
}

private fun ColorScheme.applyTonalElevation(
    backgroundColor: Color,
    elevation: Dp,
): Color =
    if (backgroundColor == surface) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
