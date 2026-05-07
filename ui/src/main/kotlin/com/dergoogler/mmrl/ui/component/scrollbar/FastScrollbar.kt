package com.dergoogler.mmrl.ui.component.scrollbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun VerticalFastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 2.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: ScrollbarColors = ScrollbarDefaults.colors(),
    thumb: @Composable () -> Unit = {
        ScrollbarDefaults.Thumb(
            color =
                colors.thumbColor(
                    canScrollForward = state.canScrollForward,
                    isScrollInProgress = state.isScrollInProgress,
                    interactionSource = interactionSource,
                ),
            orientation = Orientation.Vertical,
        )
    },
) {
    val scrollbarState = state.scrollbarState()
    val reverseLayout by remember(state) {
        derivedStateOf {
            state.layoutInfo.reverseLayout
        }
    }

    state.FastScrollbar(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(contentPadding)
                .then(modifier),
        state = scrollbarState,
        orientation = Orientation.Vertical,
        onThumbMoved = state.rememberDraggableScroller(),
        reverseLayout = reverseLayout,
        colors = colors,
        thumb = thumb,
        interactionSource = interactionSource,
    )
}

@Composable
private fun ScrollableState.FastScrollbar(
    state: ScrollbarState,
    orientation: Orientation,
    reverseLayout: Boolean,
    onThumbMoved: (Float) -> Unit,
    modifier: Modifier,
    colors: ScrollbarColors,
    thumb: @Composable () -> Unit,
    interactionSource: MutableInteractionSource,
) = Scrollbar(
    modifier = modifier,
    orientation = orientation,
    interactionSource = interactionSource,
    state = state,
    thumb = thumb,
    backgroundColor =
        colors.trackColor(
            canScrollForward = canScrollForward,
            isScrollInProgress = isScrollInProgress,
            interactionSource = interactionSource,
        ),
    onThumbMoved = onThumbMoved,
    reverseLayout = reverseLayout,
)

@Immutable
class ScrollbarColors internal constructor(
    private val contentColor: Color,
    private val activeContentColor: Color,
    private val containerColor: Color,
    private val activeContainerColor: Color,
) {
    @Composable
    private fun scrollbarColor(
        color1: Color,
        color2: Color,
        canScrollForward: Boolean,
        isScrollInProgress: Boolean,
        interactionSource: InteractionSource,
    ): Color {
        var state by remember { mutableStateOf(ThumbState.Dormant) }
        val pressed by interactionSource.collectIsPressedAsState()
        val hovered by interactionSource.collectIsHoveredAsState()
        val dragged by interactionSource.collectIsDraggedAsState()
        val active = canScrollForward && (pressed || hovered || dragged || isScrollInProgress)

        val color by animateColorAsState(
            targetValue =
                when (state) {
                    ThumbState.Active -> color1
                    ThumbState.Inactive -> color2
                    ThumbState.Dormant -> color2.copy(0f)
                },
            animationSpec = SpringSpec(stiffness = Spring.StiffnessLow),
            label = "scrollbarColor",
        )
        LaunchedEffect(active) {
            when (active) {
                true -> state = ThumbState.Active
                false ->
                    if (state == ThumbState.Active) {
                        state = ThumbState.Inactive
                        delay(SCROLLBAR_INACTIVE_TO_DORMANT_TIME_IN_MS)
                        state = ThumbState.Dormant
                    }
            }
        }

        return color
    }

    @Composable
    fun thumbColor(
        canScrollForward: Boolean,
        isScrollInProgress: Boolean,
        interactionSource: InteractionSource,
    ): Color =
        scrollbarColor(
            color1 = activeContentColor,
            color2 = contentColor,
            canScrollForward = canScrollForward,
            isScrollInProgress = isScrollInProgress,
            interactionSource = interactionSource,
        )

    @Composable
    fun trackColor(
        canScrollForward: Boolean,
        isScrollInProgress: Boolean,
        interactionSource: InteractionSource,
    ): Color =
        scrollbarColor(
            color1 = activeContainerColor,
            color2 = containerColor,
            canScrollForward = canScrollForward,
            isScrollInProgress = isScrollInProgress,
            interactionSource = interactionSource,
        )

    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ScrollbarColors) return false

        if (contentColor != other.contentColor) return false
        if (activeContentColor != other.activeContentColor) return false
        if (containerColor != other.containerColor) return false
        if (activeContainerColor != other.activeContainerColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentColor.hashCode()
        result = 31 * result + activeContentColor.hashCode()
        result = 31 * result + containerColor.hashCode()
        result = 31 * result + activeContainerColor.hashCode()

        return result
    }

    companion object {
        private const val SCROLLBAR_INACTIVE_TO_DORMANT_TIME_IN_MS = 2_000L

        private enum class ThumbState {
            Active,
            Inactive,
            Dormant,
        }
    }
}

object ScrollbarDefaults {
    @Composable
    fun colors(
        contentColor: Color = MaterialTheme.colorScheme.primary,
        scrolledContentColor: Color = contentColor,
        containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
        scrolledContainerColor: Color = containerColor,
    ) = ScrollbarColors(
        contentColor,
        scrolledContentColor,
        containerColor,
        scrolledContainerColor,
    )

    @Composable
    fun Thumb(
        color: Color,
        orientation: Orientation,
        size: Dp = 8.dp,
        shape: RoundedCornerShape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
    ) = Box(
        modifier =
            Modifier
                .run {
                    when (orientation) {
                        Orientation.Vertical -> width(size).fillMaxHeight()
                        Orientation.Horizontal -> height(size).fillMaxWidth()
                    }
                }.background(
                    color = color,
                    shape = shape,
                ),
    )
}
