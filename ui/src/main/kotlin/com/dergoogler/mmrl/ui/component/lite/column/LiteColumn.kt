package com.dergoogler.mmrl.ui.component.lite.column

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
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull

@Composable
fun LiteColumn(
    modifier: Modifier = Modifier,
    spaceBetweenItem: Dp = 0.dp,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    content: @Composable LiteColumnScope.() -> Unit,
) {
    Layout(
        content = { LiteColumnScopeInstance.content() },
        modifier = modifier,
        measurePolicy = rememberColumnMeasurePolicy(spaceBetweenItem, horizontalAlignment),
    )
}

@NonRestartableComposable
@Composable
private fun rememberColumnMeasurePolicy(
    spaceBetweenItem: Dp,
    alignment: HorizontalAlignment,
) = remember(spaceBetweenItem, alignment) {
    LiteColumnMeasurePolicy(spaceBetweenItem, alignment)
}

@Immutable
private class LiteColumnMeasurePolicy(
    private val space: Dp,
    private val parentAlignment: HorizontalAlignment,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val alignments = arrayOfNulls<HorizontalAlignment>(measurables.size)
        val spaceAsPixel = space.toPx().toInt()

        val childConstraints =
            constraints.copy(
                minWidth = 0,
                maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity,
                minHeight = 0,
                maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else Constraints.Infinity,
            )

        val hasWeightedChild =
            measurables.any {
                it.liteColumnChildDataNode?.weight?.let { weight ->
                    weight > 0f
                } ?: false
            }

        if (!hasWeightedChild) {
            val placeables =
                Array(measurables.size) { index ->

                    measurables[index].liteColumnChildDataNode?.alignment?.let {
                        alignments[index] = it
                    }
                    measurables[index].measure(childConstraints)
                }
            val totalWidth = placeables.maxOfOrNull { it.width } ?: 0
            val constrainedWidth = constraints.constrainWidth(totalWidth)
            val totalHeight =
                placeables.sumOf { it.height } +
                    (placeables.size - 1).coerceAtLeast(0) * spaceAsPixel
            val constrainedHeight = constraints.constrainHeight(totalHeight)
            var yPosition = 0
            return layout(constrainedWidth, constrainedHeight) {
                for (index in placeables.indices) {
                    val alignment = alignments.getOrNull(index)
                    val placeable = placeables.getOrNull(index) ?: continue
                    val targetAlignment = alignment ?: parentAlignment
                    val xPosition =
                        when (targetAlignment) {
                            HorizontalAlignment.Start -> 0
                            HorizontalAlignment.Center -> (constrainedWidth - placeable.width) / 2
                            HorizontalAlignment.End -> constrainedWidth - placeable.width
                        }
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    yPosition += placeable.height + spaceAsPixel
                }
            }
        } else {
            val count = measurables.size
            val placeables = arrayOfNulls<Placeable>(count)
            val weightedIndices = mutableListOf<Int>()
            var usedHeightNonWeighted = 0

            for (i in 0 until count) {
                val childData = measurables[i].liteColumnChildDataNode
                childData?.alignment?.let { alignments[i] = it }

                val hasWeight =
                    childData?.weight?.let {
                        it > 0f
                    } ?: false

                if (hasWeight) {
                    weightedIndices.add(i)
                } else {
                    val placeable = measurables[i].measure(childConstraints)
                    placeables[i] = placeable
                    usedHeightNonWeighted += placeable.height
                }
            }

            val totalSpacing = (count - 1) * spaceAsPixel
            val availableHeight = constraints.maxHeight
            val remainingHeightForWeighted =
                (availableHeight - usedHeightNonWeighted - totalSpacing).coerceAtLeast(0)
            val totalWeight =
                weightedIndices
                    .sumOf {
                        (measurables[it].liteColumnChildDataNode?.weight ?: 0f).toDouble()
                    }.toFloat()

            for (i in weightedIndices) {
                measurables[i].liteColumnChildDataNode?.weight?.let {
                    val allocatedHeight =
                        if (totalWeight > 0f) {
                            (remainingHeightForWeighted * (it / totalWeight)).toInt()
                        } else {
                            0
                        }
                    val weightedConstraints =
                        childConstraints.copy(
                            minHeight = allocatedHeight,
                            maxHeight = allocatedHeight,
                        )
                    val placeable = measurables[i].measure(weightedConstraints)
                    placeables[i] = placeable
                }
            }

            val measuredHeights = placeables.map { it?.height ?: 0 }
            val totalHeight = measuredHeights.sum() + totalSpacing
            val totalWidth = placeables.maxOfOrNull { it?.width ?: 0 } ?: 0
            val constrainedWidth = constraints.constrainWidth(totalWidth)
            val constrainedHeight = constraints.constrainHeight(totalHeight)

            var yPosition = 0
            return layout(constrainedWidth, constrainedHeight) {
                for (index in 0 until count) {
                    val childData = alignments[index]
                    val placeable = placeables.getOrNull(index) ?: continue
                    val targetAlignment = childData ?: parentAlignment
                    val xPosition =
                        when (targetAlignment) {
                            HorizontalAlignment.Start -> 0
                            HorizontalAlignment.Center -> (constrainedWidth - placeable.width) / 2
                            HorizontalAlignment.End -> constrainedWidth - placeable.width
                        }
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    yPosition += placeable.height + spaceAsPixel
                }
            }
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int = measurables.fastMaxOfOrNull { maxIntrinsicWidthLambda(it, height) } ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int = measurables.fastMaxOfOrNull { minIntrinsicWidthLambda(it, height) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int {
        val childHeights = measurables.fastMap { maxIntrinsicHeightLambda(it, width) }
        val totalSpace = (childHeights.size - 1).coerceAtLeast(0) * space.roundToPx()
        return childHeights.sum() + totalSpace
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int {
        val childHeights = measurables.fastMap { minIntrinsicHeightLambda(it, width) }
        val totalSpace = (childHeights.size - 1).coerceAtLeast(0) * space.roundToPx()
        return childHeights.sum() + totalSpace
    }
}

private val maxIntrinsicWidthLambda: IntrinsicMeasurable.(Int) -> Int = { maxIntrinsicWidth(it) }
private val minIntrinsicWidthLambda: IntrinsicMeasurable.(Int) -> Int = { minIntrinsicWidth(it) }
private val maxIntrinsicHeightLambda: IntrinsicMeasurable.(Int) -> Int = { maxIntrinsicHeight(it) }
private val minIntrinsicHeightLambda: IntrinsicMeasurable.(Int) -> Int = { minIntrinsicHeight(it) }
