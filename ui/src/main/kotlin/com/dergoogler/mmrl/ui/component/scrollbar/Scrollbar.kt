package com.dergoogler.mmrl.ui.component.scrollbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.max
import kotlin.math.min

/**
 * The delay between scrolls when a user long presses on the scrollbar track to initiate a scroll
 * instead of dragging the scrollbar thumb.
 */
private const val SCROLLBAR_PRESS_DELAY_MS = 10L

/**
 * The percentage displacement of the scrollbar when scrolled by long presses on the scrollbar
 * track.
 */
private const val SCROLLBAR_PRESS_DELTA_PCT = 0.02f

/**
 * Class definition for the core properties of a scroll bar
 */
@Immutable
@JvmInline
value class ScrollbarState internal constructor(
    internal val packedValue: Long,
) {
    companion object {
        val FULL =
            ScrollbarState(
                thumbSizePercent = 1f,
                thumbMovedPercent = 0f,
            )
    }
}

/**
 * Class definition for the core properties of a scroll bar track
 */
@Immutable
@JvmInline
private value class ScrollbarTrack(
    val packedValue: Long,
) {
    constructor(
        max: Float,
        min: Float,
    ) : this(packFloats(max, min))
}

/**
 * Creates a [ScrollbarState] with the listed properties
 * @param thumbSizePercent the thumb size of the scrollbar as a percentage of the total track size.
 *  Refers to either the thumb width (for horizontal scrollbars)
 *  or height (for vertical scrollbars).
 * @param thumbMovedPercent the distance the thumb has traveled as a percentage of total
 * track size.
 */
fun ScrollbarState(
    thumbSizePercent: Float,
    thumbMovedPercent: Float,
) = ScrollbarState(
    packedValue =
        packFloats(
            val1 = thumbSizePercent,
            val2 = thumbMovedPercent,
        ),
)

/**
 * Returns the thumb size of the scrollbar as a percentage of the total track size
 */
val ScrollbarState.thumbSizePercent
    get() = unpackFloat1(packedValue)

/**
 * Returns the distance the thumb has traveled as a percentage of total track size
 */
val ScrollbarState.thumbMovedPercent
    get() = unpackFloat2(packedValue)

/**
 * Returns the size of the scrollbar track in pixels
 */
private val ScrollbarTrack.size
    get() = unpackFloat2(packedValue) - unpackFloat1(packedValue)

/**
 * Returns the position of the scrollbar thumb on the track as a percentage
 */
private fun ScrollbarTrack.thumbPosition(dimension: Float): Float =
    max(
        a =
            min(
                a = dimension / size,
                b = 1f,
            ),
        b = 0f,
    )

/**
 * Returns the value of [offset] along the axis specified by [this]
 */
internal fun Orientation.valueOf(offset: Offset) =
    when (this) {
        Orientation.Horizontal -> offset.x
        Orientation.Vertical -> offset.y
    }

/**
 * Returns the value of [intSize] along the axis specified by [this]
 */
internal fun Orientation.valueOf(intSize: IntSize) =
    when (this) {
        Orientation.Horizontal -> intSize.width
        Orientation.Vertical -> intSize.height
    }

/**
 * Returns the value of [intOffset] along the axis specified by [this]
 */
internal fun Orientation.valueOf(intOffset: IntOffset) =
    when (this) {
        Orientation.Horizontal -> intOffset.x
        Orientation.Vertical -> intOffset.y
    }

/**
 * A Composable for drawing a scrollbar
 * @param orientation the scroll direction of the scrollbar
 * @param state the state describing the position of the scrollbar
 * @param backgroundColor the color of the background for scrollbar
 * @param thumbSize the size of the scrollbar thumb
 * @param interactionSource allows for observing the state of the scroll bar
 * @param thumb a composable for drawing the scrollbar thumb
 * @param onThumbMoved an function for reacting to scroll bar displacements caused by direct
 * interactions on the scrollbar thumb by the user, for example implementing a fast scroll
 * @param reverseLayout reverse the direction of scrolling and layout
 */
@Composable
fun Scrollbar(
    modifier: Modifier,
    orientation: Orientation,
    state: ScrollbarState,
    backgroundColor: Color,
    thumbSize: Dp = 50.dp,
    interactionSource: MutableInteractionSource?,
    thumb: @Composable () -> Unit,
    onThumbMoved: ((Float) -> Unit)?,
    shape: RoundedCornerShape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
    reverseLayout: Boolean,
) {
    // Only displayed when thumb size > 0.50
    if (state.thumbSizePercent > 0.50) return

    // Using Offset.Unspecified and Float.NaN instead of null
    // to prevent unnecessary boxing of primitives
    var pressedOffset by remember { mutableStateOf(Offset.Unspecified) }
    var draggedOffset by remember { mutableStateOf(Offset.Unspecified) }

    // Used to immediately show drag feedback in the UI while the scrolling implementation
    // catches up
    var interactionThumbTravelPercent by remember { mutableFloatStateOf(Float.NaN) }

    var track by remember { mutableStateOf(ScrollbarTrack(packedValue = 0)) }

    val thumbSizePx = with(LocalDensity.current) { thumbSize.toPx() }
    val oldThumbSizePx = state.thumbSizePercent * track.size

    val thumbTravelPercent =
        when {
            interactionThumbTravelPercent.isNaN() -> {
                // Calculate the resize percentage
                val p = (track.size - thumbSizePx) / (track.size - oldThumbSizePx)
                val new = state.thumbMovedPercent * p

                val thumbMovedPercent =
                    when {
                        new.isNaN() -> 0f
                        new.isInfinite() -> 1f
                        else -> new
                    }

                when {
                    reverseLayout -> 1f - thumbMovedPercent
                    else -> thumbMovedPercent
                }
            }

            else -> interactionThumbTravelPercent
        }

    val thumbSizeDp by animateDpAsState(
        targetValue = with(LocalDensity.current) { thumbSizePx.toDp() },
        label = "thumbSizeDp",
    )

    val thumbMovedPx =
        min(
            a =
                when {
                    reverseLayout -> track.size * thumbTravelPercent - thumbSizePx
                    else -> track.size * thumbTravelPercent
                },
            b = track.size - thumbSizePx,
        )

    // scrollbar track container
    Box(
        modifier =
            modifier
                .run {
                    val withHover = interactionSource?.let(::hoverable) ?: this
                    when (orientation) {
                        Orientation.Vertical -> withHover.fillMaxHeight()
                        Orientation.Horizontal -> withHover.fillMaxWidth()
                    }
                }.onGloballyPositioned { coordinates ->
                    val scrollbarStartCoordinate = orientation.valueOf(coordinates.positionInRoot())
                    track =
                        ScrollbarTrack(
                            max = scrollbarStartCoordinate,
                            min = scrollbarStartCoordinate + orientation.valueOf(coordinates.size),
                        )
                }
                // Process scrollbar presses
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            try {
                                // Wait for a long press before scrolling
                                withTimeout(viewConfiguration.longPressTimeoutMillis) {
                                    tryAwaitRelease()
                                }
                            } catch (e: TimeoutCancellationException) {
                                // Start the press triggered scroll
                                val initialPress = PressInteraction.Press(offset)
                                interactionSource?.tryEmit(initialPress)

                                pressedOffset = offset
                                interactionSource?.tryEmit(
                                    when {
                                        tryAwaitRelease() -> PressInteraction.Release(initialPress)
                                        else -> PressInteraction.Cancel(initialPress)
                                    },
                                )

                                // End the press
                                pressedOffset = Offset.Unspecified
                            }
                        },
                    )
                }
                // Process scrollbar drags
                .pointerInput(Unit) {
                    var dragInteraction: DragInteraction.Start? = null
                    val onDragStart: (Offset) -> Unit = { offset ->
                        val start = DragInteraction.Start()
                        dragInteraction = start
                        interactionSource?.tryEmit(start)
                        draggedOffset = offset
                    }
                    val onDragEnd: () -> Unit = {
                        dragInteraction?.let { interactionSource?.tryEmit(DragInteraction.Stop(it)) }
                        draggedOffset = Offset.Unspecified
                    }
                    val onDragCancel: () -> Unit = {
                        dragInteraction?.let { interactionSource?.tryEmit(DragInteraction.Cancel(it)) }
                        draggedOffset = Offset.Unspecified
                    }
                    val onDrag: (change: PointerInputChange, dragAmount: Float) -> Unit =
                        onDrag@{ _, delta ->
                            if (draggedOffset == Offset.Unspecified) return@onDrag
                            draggedOffset =
                                when (orientation) {
                                    Orientation.Vertical ->
                                        draggedOffset.copy(
                                            y = draggedOffset.y + delta,
                                        )

                                    Orientation.Horizontal ->
                                        draggedOffset.copy(
                                            x = draggedOffset.x + delta,
                                        )
                                }
                        }

                    when (orientation) {
                        Orientation.Horizontal ->
                            detectHorizontalDragGestures(
                                onDragStart = onDragStart,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel,
                                onHorizontalDrag = onDrag,
                            )

                        Orientation.Vertical ->
                            detectVerticalDragGestures(
                                onDragStart = onDragStart,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel,
                                onVerticalDrag = onDrag,
                            )
                    }
                }.background(
                    color = backgroundColor,
                    shape = shape,
                ),
    ) {
        val scrollbarThumbMovedDp =
            max(
                a = with(LocalDensity.current) { thumbMovedPx.toDp() },
                b = 0.dp,
            )
        // scrollbar thumb container
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .run {
                        when (orientation) {
                            Orientation.Horizontal -> width(thumbSizeDp)
                            Orientation.Vertical -> height(thumbSizeDp)
                        }
                    }.offset(
                        y =
                            when (orientation) {
                                Orientation.Horizontal -> 0.dp
                                Orientation.Vertical -> scrollbarThumbMovedDp
                            },
                        x =
                            when (orientation) {
                                Orientation.Horizontal -> scrollbarThumbMovedDp
                                Orientation.Vertical -> 0.dp
                            },
                    ),
        ) {
            thumb()
        }
    }

    if (onThumbMoved == null) return
    val thumbMoved: (Float) -> Unit = {
        onThumbMoved(
            when {
                reverseLayout -> 1f - it
                else -> it
            },
        )
    }

    // State that will be read inside the effects that follow
    // but will not cause re-triggering of them
    val updatedState by rememberUpdatedState(state)

    // Process presses
    LaunchedEffect(pressedOffset) {
        // Press ended, reset interactionThumbTravelPercent
        if (pressedOffset == Offset.Unspecified) {
            interactionThumbTravelPercent = Float.NaN
            return@LaunchedEffect
        }

        var currentThumbMovedPercent = updatedState.thumbMovedPercent
        val destinationThumbMovedPercent =
            track.thumbPosition(
                dimension = orientation.valueOf(pressedOffset),
            )
        val isPositive = currentThumbMovedPercent < destinationThumbMovedPercent
        val delta = SCROLLBAR_PRESS_DELTA_PCT * if (isPositive) 1f else -1f

        while (currentThumbMovedPercent != destinationThumbMovedPercent) {
            currentThumbMovedPercent =
                when {
                    isPositive ->
                        min(
                            a = currentThumbMovedPercent + delta,
                            b = destinationThumbMovedPercent,
                        )

                    else ->
                        max(
                            a = currentThumbMovedPercent + delta,
                            b = destinationThumbMovedPercent,
                        )
                }

            thumbMoved(currentThumbMovedPercent)

            interactionThumbTravelPercent = currentThumbMovedPercent
            delay(SCROLLBAR_PRESS_DELAY_MS)
        }
    }

    // Process drags
    LaunchedEffect(draggedOffset) {
        if (draggedOffset == Offset.Unspecified) {
            interactionThumbTravelPercent = Float.NaN
            return@LaunchedEffect
        }

        val currentTravel =
            track.thumbPosition(
                dimension = orientation.valueOf(draggedOffset),
            )

        thumbMoved(currentTravel)
        interactionThumbTravelPercent = currentTravel
    }
}
