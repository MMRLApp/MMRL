package com.dergoogler.mmrl.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.applyAlpha
import com.dergoogler.mmrl.ui.token.applyTonalElevation

/**
 * A Composable function that creates a card with customizable appearance and behavior.
 *
 * This card supports relative and absolute positioning of its content, click and long-click
 * interactions, and Material Design styling options like color, shape, elevation, and border.
 *
 * @param modifier The modifier to be applied to the card.
 * @param enabled Controls the enabled state of the card. When `false`, the card will appear
 * dimmed and will not be interactive.
 * @param color The background color of the card.
 * @param contentColor The preferred color for content inside the card.
 * @param shape The shape of the card's container.
 * @param tonalElevation The tonal elevation of the card. This is used to apply a subtle overlay
 * color that indicates the card's elevation.
 * @param shadowElevation The shadow elevation of the card. This creates a visual shadow effect
 * beneath the card.
 * @param border Optional border to draw around the card.
 * @param interactionSource The [MutableInteractionSource] representing the stream of
 * [Interaction]s for this card. You can create and pass in your own `remember`ed
 * instance to observe [Interaction]s and customize the appearance / behavior of this card in
 * different states.
 * @param onClick Optional lambda to be invoked when the card is clicked.
 * @param onLongClick Optional lambda to be invoked when the card is long-clicked.
 * @param content The content to be displayed inside the card. This lambda receives a [CardScope]
 * which provides modifiers for positioning child elements (e.g., `Modifier.relative()`,
 * `Modifier.absolute()`).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Card(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shape: Shape = RoundedCornerShape(20.dp),
    outsideContentPadding: PaddingValues = PaddingValues(0.dp),
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable CardScope.() -> Unit,
) {
    val instance = remember { CardScopeInstance() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation

    val clickableModifier = if (onClick != null || onLongClick != null) {
        Modifier
            .combinedClickable(
                enabled = enabled,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .hoverable(interactionSource)
    } else Modifier

    val hoveredBorder = if (isHovered) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else border

    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        Layout(
            modifier = Modifier
                .padding(outsideContentPadding)
                .card(
                    shape = shape,
                    backgroundColor = MaterialTheme.colorScheme.applyTonalElevation(
                        color,
                        absoluteElevation
                    ),
                    border = hoveredBorder,
                    shadowElevation = with(LocalDensity.current) { shadowElevation.toPx() }
                )
                .applyAlpha(enabled)
                .semantics(mergeDescendants = false) {
                    @Suppress("DEPRECATION")
                    isContainer = true
                }
                .then(clickableModifier)
                .pointerInput(Unit) {}
                .then(modifier),
            content = { instance.content() }
        ) { measurables, constraints ->
            val relativeMeasurable = measurables.firstOrNull {
                (it.layoutId as? CardLayoutId)?.slot == CardSlot.Relative
            }
            val absoluteMeasurables = measurables.filter {
                (it.layoutId as? CardLayoutId)?.slot == CardSlot.Absolute
            }

            val relativePlaceable = relativeMeasurable?.measure(constraints)
            val absolutePlaceables = absoluteMeasurables.map { it to it.measure(constraints) }

            val width = maxOf(
                relativePlaceable?.width ?: 0,
                absolutePlaceables.maxOfOrNull { it.second.width } ?: 0
            )
            val height = maxOf(
                relativePlaceable?.height ?: 0,
                absolutePlaceables.maxOfOrNull { it.second.height } ?: 0
            )

            layout(width, height) {
                relativePlaceable?.placeRelative(0, 0)

                absolutePlaceables.forEach { (measurable, placeable) ->
                    val layoutId = measurable.layoutId as? CardLayoutId
                    val alignment = layoutId?.alignment ?: Alignment.TopStart

                    val childSize = IntSize(placeable.width, placeable.height)
                    val parentSize = IntSize(width, height)

                    val position = alignment.align(
                        size = childSize,
                        space = parentSize,
                        layoutDirection = layoutDirection
                    )

                    placeable.place(position.x, position.y)
                }
            }
        }
    }
}

fun Modifier.card(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.Transparent,
    border: BorderStroke? = null,
    shadowElevation: Float? = null,
) = this
    .then(
        if (shadowElevation != null && shadowElevation > 0f) {
            Modifier.graphicsLayer(
                shadowElevation = shadowElevation,
                shape = shape,
                clip = false
            )
        } else Modifier
    )
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)
