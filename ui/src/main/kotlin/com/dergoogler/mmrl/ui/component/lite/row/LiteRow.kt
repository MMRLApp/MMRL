package com.dergoogler.mmrl.ui.component.lite.row

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull

@Composable
fun LiteRow(
    modifier: Modifier = Modifier,
    spaceBetweenItem: Dp = 0.dp,
    verticalAlignment: VerticalAlignment = VerticalAlignment.Top,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    content: @Composable LiteRowScope.() -> Unit,
) {
    Layout(
        content = { LiteRowScopeInstance.content() },
        modifier = modifier,
        measurePolicy =
            rememberLiteRowMeasurePolicy(
                spaceBetweenItem,
                verticalAlignment,
                horizontalAlignment,
            ),
    )
}

@NonRestartableComposable
@Composable
private fun rememberLiteRowMeasurePolicy(
    spaceBetweenItem: Dp,
    verticalAlignment: VerticalAlignment,
    horizontalAlignment: HorizontalAlignment,
) = remember(spaceBetweenItem, verticalAlignment, horizontalAlignment) {
    LiteRowMeasurePolicy(spaceBetweenItem, verticalAlignment, horizontalAlignment)
}

@Immutable
private class LiteRowMeasurePolicy(
    private val spaceBetweenItem: Dp,
    private val verticalAlignmentParent: VerticalAlignment,
    private val horizontalAlignmentParent: HorizontalAlignment,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val spaceAsPx = spaceBetweenItem.toPx().toInt()
        val alignments = arrayOfNulls<LiteRowChildData>(measurables.size)
        val availableWidth = constraints.maxWidth

        val hasWeightedChild =
            measurables.any {
                it.liteRowChildDataNode?.weight?.let { weight ->
                    weight > 0f
                } ?: false
            }
        if (!hasWeightedChild) {
            var usedWidth = 0
            val placeables =
                Array(measurables.size) { index ->
                    alignments[index] = measurables[index].liteRowChildDataNode
                    val remainingWidth =
                        availableWidth - usedWidth - ((measurables.size - 1 - index) * spaceAsPx)
                    val adjustedConstraints =
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = remainingWidth.coerceIn(0, constraints.maxWidth),
                        )
                    val placeable = measurables[index].measure(adjustedConstraints)
                    usedWidth += placeable.width + spaceAsPx
                    placeable
                }
            val totalWidth = usedWidth - spaceAsPx.coerceAtLeast(0)
            val totalHeight = placeables.maxOfOrNull { it.height } ?: 0

            var xPosition = 0
            return layout(
                totalWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
                totalHeight,
            ) {
                for (index in placeables.indices) {
                    val alignment = alignments.getOrNull(index)
                    val placeable = placeables.getOrNull(index) ?: continue
                    val targetVerticalAlignment =
                        alignment?.verticalAlignment ?: verticalAlignmentParent
                    val yPosition =
                        when (targetVerticalAlignment) {
                            VerticalAlignment.Top -> 0
                            VerticalAlignment.Center -> (totalHeight - placeable.height) / 2
                            VerticalAlignment.Bottom -> totalHeight - placeable.height
                        }
                    val targetHorizontalAlignment =
                        alignment?.horizontalAlignment ?: horizontalAlignmentParent

                    val xPositionAdjusted =
                        when (targetHorizontalAlignment) {
                            HorizontalAlignment.Start -> xPosition
                            HorizontalAlignment.Center -> {
                                if (!constraints.hasFixedWidth) {
                                    xPosition
                                } else {
                                    (constraints.maxWidth - totalWidth) / 2 + xPosition
                                }
                            }

                            HorizontalAlignment.End -> {
                                if (!constraints.hasFixedWidth) {
                                    xPosition
                                } else {
                                    constraints.maxWidth - totalWidth + xPosition
                                }
                            }
                        }
                    placeable.placeRelative(x = xPositionAdjusted, y = yPosition)
                    xPosition += placeable.width + spaceAsPx
                }
            }
        } else {
            val count = measurables.size
            val placeables = arrayOfNulls<Placeable>(count)
            val totalSpacing = (count - 1) * spaceAsPx

            var usedWidthNonWeighted = 0
            val weightedIndices = mutableListOf<Int>()
            for (i in 0 until count) {
                val childData = measurables[i].liteRowChildDataNode
                alignments[i] = childData
                if ((childData?.weight ?: 0f) > 0f) {
                    weightedIndices.add(i)
                } else {
                    val remainingCount = count - i - 1
                    val remainingWidth =
                        availableWidth - usedWidthNonWeighted - (remainingCount * spaceAsPx)
                    val adjustedConstraints =
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = remainingWidth.coerceIn(0, constraints.maxWidth),
                        )
                    val placeable = measurables[i].measure(adjustedConstraints)
                    placeables[i] = placeable
                    usedWidthNonWeighted += placeable.width + spaceAsPx
                }
            }

            var totalNonWeightedWidth = 0
            for (i in 0 until count) {
                if ((measurables[i].liteRowChildDataNode?.weight ?: 0f) <= 0f) {
                    totalNonWeightedWidth += placeables[i]?.width ?: 0
                }
            }

            val remainingWidthForWeighted =
                (availableWidth - totalNonWeightedWidth - totalSpacing).coerceAtLeast(0)
            val totalWeight =
                weightedIndices
                    .sumOf {
                        measurables[it].liteRowChildDataNode?.weight?.toDouble() ?: 0.0
                    }.toFloat()

            for (i in weightedIndices) {
                val weight = measurables[i].liteRowChildDataNode?.weight ?: 0f
                val allocatedWidth =
                    if (totalWeight > 0f) {
                        (remainingWidthForWeighted * (weight / totalWeight)).toInt()
                    } else {
                        0
                    }
                val fill = measurables[i].liteRowChildDataNode?.fill ?: true
                val childConstraints =
                    if (fill) {
                        constraints.copy(minWidth = allocatedWidth, maxWidth = allocatedWidth)
                    } else {
                        constraints.copy(minWidth = 0, maxWidth = allocatedWidth)
                    }
                val placeable = measurables[i].measure(childConstraints)
                placeables[i] = placeable
            }

            val measuredWidths = placeables.map { it?.width ?: 0 }
            val totalWidth = measuredWidths.sum() + totalSpacing
            val totalHeight = placeables.maxOfOrNull { it?.height ?: 0 } ?: 0

            var xPosition = 0
            return layout(
                totalWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
                totalHeight,
            ) {
                for (index in 0 until count) {
                    val childData = alignments[index]
                    val placeable = placeables.getOrNull(index) ?: continue
                    val targetVerticalAlignment =
                        childData?.verticalAlignment ?: verticalAlignmentParent
                    val yPosition =
                        when (targetVerticalAlignment) {
                            VerticalAlignment.Top -> 0
                            VerticalAlignment.Center -> (totalHeight - placeable.height) / 2
                            VerticalAlignment.Bottom -> totalHeight - placeable.height
                        }
                    val targetHorizontalAlignment =
                        childData?.horizontalAlignment ?: horizontalAlignmentParent

                    val xPositionAdjusted =
                        when (targetHorizontalAlignment) {
                            HorizontalAlignment.Start -> xPosition
                            HorizontalAlignment.Center -> {
                                if (!constraints.hasFixedWidth) {
                                    xPosition
                                } else {
                                    (constraints.maxWidth - totalWidth) / 2 + xPosition
                                }
                            }

                            HorizontalAlignment.End -> {
                                if (!constraints.hasFixedWidth) {
                                    xPosition
                                } else {
                                    constraints.maxWidth - totalWidth + xPosition
                                }
                            }
                        }
                    placeable.placeRelative(x = xPositionAdjusted, y = yPosition)
                    xPosition += placeable.width + spaceAsPx
                }
            }
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = measurables.fastMaxOfOrNull { it.maxIntrinsicHeightLambda(width) } ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = measurables.fastMaxOfOrNull { it.minIntrinsicHeightLambda(width) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int {
        val childWidths = measurables.fastMap { it.maxIntrinsicWidthLambda(height) }
        val totalGapWidth = (childWidths.size - 1).coerceAtLeast(0) * spaceBetweenItem.roundToPx()
        return childWidths.sum() + totalGapWidth
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int {
        val childWidths = measurables.fastMap { it.minIntrinsicWidthLambda(height) }
        val totalGapWidth = (childWidths.size - 1).coerceAtLeast(0) * spaceBetweenItem.roundToPx()
        return childWidths.sum() + totalGapWidth
    }
}

private val maxIntrinsicWidthLambda: IntrinsicMeasurable.(Int) -> Int = { maxIntrinsicWidth(it) }
private val minIntrinsicWidthLambda: IntrinsicMeasurable.(Int) -> Int = { minIntrinsicWidth(it) }
private val maxIntrinsicHeightLambda: IntrinsicMeasurable.(Int) -> Int = { maxIntrinsicHeight(it) }
private val minIntrinsicHeightLambda: IntrinsicMeasurable.(Int) -> Int = { minIntrinsicHeight(it) }
