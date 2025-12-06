package com.dergoogler.mmrl.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    shape: Shape = SegmentedButtonsDefaults.Shape,
    border: BorderStroke = SegmentedButtonsDefaults.border(),
    segments: @Composable () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .selectableGroup()
                .defaultMinSize(minHeight = 40.dp),
        shape = shape,
        contentColor = contentColor,
        color = containerColor,
        border = border,
    ) {
        SubcomposeLayout { constraints ->
            val segmentsMeasurable = subcompose(SegmentSlots.Segment, segments)
            val segmentCount = segmentsMeasurable.size
            val segmentsPlaceable =
                segmentsMeasurable.map {
                    val width = it.maxIntrinsicWidth(constraints.maxHeight)
                    it.measure(constraints.copy(minWidth = 0, maxWidth = width))
                }

            val segmentedButtonsHeight = segmentsPlaceable.maxBy { it.height }.height
            val segmentedButtonsWidth = segmentsPlaceable.sumOf { it.width }

            val divider =
                SegmentedButtonsDefaults.divider(
                    height = segmentedButtonsHeight.toDp(),
                    border = border,
                )
            val dividerWidth = border.width.roundToPx()

            layout(segmentedButtonsWidth, segmentedButtonsHeight) {
                var x = 0
                segmentsPlaceable.forEachIndexed { index, placeableItem ->
                    placeableItem.placeRelative(x, 0)
                    x += placeableItem.width

                    if (index == segmentCount - 1) return@forEachIndexed

                    val dividerMeasurable = subcompose(index, divider)
                    val placeableDivider =
                        dividerMeasurable
                            .first()
                            .measure(constraints.copy(minWidth = 0))

                    placeableDivider.placeRelative(x, 0)
                    x += dividerWidth
                }
            }
        }
    }
}

@Composable
fun Segment(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SegmentColors = SegmentedButtonsDefaults.buttonColor(),
    contentPadding: PaddingValues = SegmentedButtonsDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    icon: (@Composable () -> Unit)? = { SegmentedButtonsDefaults.SegmentIcon(selected) },
    content: @Composable RowScope.() -> Unit,
) {
    val containerColor by colors.containerColor(selected)
    val contentColor by colors.contentColor(selected)

    Surface(
        modifier =
            modifier
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication =
                        ripple(
                            bounded = true,
                            color = colors.containerColor(selected).value,
                        ),
                ),
        color = containerColor,
        contentColor = contentColor,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) icon()
                    if (icon != null && selected) {
                        Spacer(modifier = Modifier.width(SegmentedButtonsDefaults.IconSpacing))
                    }

                    content()
                }
            }
        }
    }
}

private enum class SegmentSlots {
    Segment,
}

object SegmentedButtonsDefaults {
    val IconSize = 18.dp
    val IconSpacing = 8.dp
    val Shape: Shape = CircleShape
    val ContentPadding = PaddingValues(vertical = 8.dp, horizontal = 24.dp)

    @Composable
    fun border(
        width: Dp = 1.dp,
        color: Color = MaterialTheme.colorScheme.outline,
    ) = BorderStroke(
        width = width,
        color = color,
    )

    @Composable
    fun buttonColor(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        selectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    ) = SegmentColors(
        containerColor = containerColor,
        contentColor = contentColor,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
    )

    fun divider(
        height: Dp,
        border: BorderStroke,
    ) = @Composable {
        Canvas(
            modifier =
                Modifier
                    .height(height)
                    .width(border.width),
        ) {
            drawLine(
                brush = border.brush,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = size.width,
            )
        }
    }

    @Composable
    fun ActiveIcon() {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(IconSize),
        )
    }

    @Composable
    fun SegmentIcon(
        active: Boolean,
        activeContent: @Composable () -> Unit = { ActiveIcon() },
        inactiveContent: (@Composable () -> Unit)? = null,
    ) {
        if (inactiveContent == null) {
            AnimatedVisibility(
                visible = active,
                exit = ExitTransition.None,
                enter =
                    fadeIn(tween(350)) +
                        scaleIn(
                            initialScale = 0f,
                            transformOrigin = TransformOrigin(0f, 1f),
                            animationSpec = tween(350),
                        ),
            ) {
                activeContent()
            }
        } else {
            Crossfade(
                targetState = active,
                label = "SegmentIcon",
            ) {
                if (it) activeContent() else inactiveContent()
            }
        }
    }
}

@Immutable
class SegmentColors internal constructor(
    private val containerColor: Color,
    private val contentColor: Color,
    private val selectedContainerColor: Color,
    private val selectedContentColor: Color,
) {
    @Composable
    internal fun containerColor(selected: Boolean): State<Color> =
        rememberUpdatedState(if (selected) selectedContainerColor else containerColor)

    @Composable
    internal fun contentColor(selected: Boolean): State<Color> = rememberUpdatedState(if (selected) selectedContentColor else contentColor)

    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SegmentColors) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (selectedContainerColor != other.selectedContainerColor) return false
        if (selectedContentColor != other.selectedContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + selectedContainerColor.hashCode()
        result = 31 * result + selectedContentColor.hashCode()
        return result
    }
}
