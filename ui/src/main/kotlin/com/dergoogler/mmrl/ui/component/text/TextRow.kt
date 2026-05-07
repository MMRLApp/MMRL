package com.dergoogler.mmrl.ui.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable

/**
 * A composable function that arranges content in a row with optional leading and trailing content.
 *
 * This function provides a flexible way to lay out content horizontally, with the ability to
 * include content before (leading) and after (trailing) the main content. It also allows for
 * control over horizontal arrangement and vertical alignment.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param horizontalArrangement The horizontal arrangement of the content within the row. Defaults to `Arrangement.Start`.
 * @param verticalAlignment The vertical alignment of the content within the row. Defaults to `Alignment.CenterVertically`.
 * @param leadingContent Optional composable function representing the content to be placed before the main content.
 *                      If provided, it will be wrapped in a `Box` with padding to the right.
 * @param trailingContent Optional composable function representing the content to be placed after the main content.
 *                       If provided, it will be wrapped in a `Box` with padding to the left.
 * @param content The main composable function representing the content to be placed between the leading and trailing content.
 *
 * Example Usage:
 * ```
 * TextRow(
 *     leadingContent = { Icon(Icons.Filled.Info, contentDescription = "Info") },
 *     trailingContent = { Text("Trailing") },
 * ) {
 *     Text("Main Content")
 * }
 * ```
 */
@Composable
fun TextRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    contentPadding: PaddingValues = PaddingValues(start = 8.dp, end = 8.dp),
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val decoratedLeadingContent: @Composable (() -> Unit)? =
        leadingContent.nullable {
            {
                Box(
                    modifier =
                        Modifier.padding(
                            end =
                                contentPadding.calculateEndPadding(
                                    layoutDirection,
                                ),
                        ),
                ) {
                    it()
                }
            }
        }

    val decoratedTrailingContent: @Composable (() -> Unit)? =
        trailingContent.nullable {
            {
                Box(
                    modifier =
                        Modifier.padding(
                            start =
                                contentPadding.calculateStartPadding(
                                    layoutDirection,
                                ),
                        ),
                ) {
                    it()
                }
            }
        }

    TextRowLayout(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        leadingContent = decoratedLeadingContent,
        content = content,
        trailingContent = decoratedTrailingContent,
    )
}

@Composable
private fun TextRowLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    leadingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val measurePolicy =
        remember(horizontalArrangement, verticalAlignment) {
            TextRowMeasurePolicy(horizontalArrangement, verticalAlignment)
        }

    Layout(
        modifier = modifier,
        contents =
            listOf(
                leadingContent ?: {},
                content,
                trailingContent ?: {},
            ),
        measurePolicy = measurePolicy,
    )
}

private class TextRowMeasurePolicy(
    private val horizontalArrangement: Arrangement.Horizontal,
    private val verticalAlignment: Alignment.Vertical,
) : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<List<Measurable>>,
        constraints: Constraints,
    ): MeasureResult {
        val flattenedMeasurables = measurables.flatten()
        val placeables = flattenedMeasurables.map { it.measure(constraints) }

        val totalWidth =
            placeables.sumOf { it.width }.coerceIn(constraints.minWidth, constraints.maxWidth)
        val maxHeight =
            placeables
                .maxOfOrNull { it.height }
                ?.coerceIn(constraints.minHeight, constraints.maxHeight) ?: 0

        val outPositions = IntArray(placeables.size)
        with(horizontalArrangement) {
            arrange(
                totalSize = totalWidth,
                sizes = placeables.map { it.width }.toIntArray(),
                layoutDirection = LayoutDirection.Ltr,
                outPositions = outPositions,
            )
        }

        return layout(totalWidth, maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val y = verticalAlignment.align(placeable.height, maxHeight)
                placeable.placeRelative(outPositions[index], y)
            }
        }
    }
}
