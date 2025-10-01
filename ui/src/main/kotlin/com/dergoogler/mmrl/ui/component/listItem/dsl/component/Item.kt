package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScopeInstance
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.LocalListItemEnabled
import kotlin.math.max

@Composable
fun ListScope.Item(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = contentPaddingValues,
    content: @Composable ListItemScope.() -> Unit,
) {
    val instance = remember {
        ListItemScopeInstance(
            contentPaddingValues = contentPadding,
            iconSize = iconSize
        )
    }

    val layoutDirection = LocalLayoutDirection.current

    CompositionLocalProvider(
        LocalListItemEnabled provides enabled,
    ) {
        Layout(
            content = { instance.content() },
            modifier = modifier
                .alpha(if (enabled) 1f else 0.5f)
                .padding(instance.contentPaddingValues),
        ) { measurables, constraints ->

            val startMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.Start }
            val titleMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.Title }
            val descriptionMeasurable =
                measurables.firstOrNull { it.layoutId == ListItemSlot.Description }
            val supportingMeasurable =
                measurables.firstOrNull { it.layoutId == ListItemSlot.Supporting }
            val endMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.End }

            val startPlaceable = startMeasurable?.measure(constraints)
            val startWidth = startPlaceable?.width ?: 0
            val startPaddingPx =
                instance.contentPaddingValues.calculateStartPadding(layoutDirection).roundToPx()
            val startSpacerWidth = if (startPlaceable != null) startPaddingPx else 0

            val endPlaceable = endMeasurable?.measure(constraints)
            val endWidth = endPlaceable?.width ?: 0
            val endPaddingPx =
                instance.contentPaddingValues.calculateEndPadding(layoutDirection).roundToPx()
            val endSpacerWidth = if (endPlaceable != null) endPaddingPx else 0

            val textMaxWidth =
                constraints.maxWidth - startWidth - startSpacerWidth - endWidth - endSpacerWidth
            val textConstraints = constraints.copy(
                minWidth = 0,
                maxWidth = max(0, textMaxWidth)
            )

            val titlePlaceable = titleMeasurable?.measure(textConstraints)
            val descriptionPlaceable = descriptionMeasurable?.measure(textConstraints)
            val supportingPlaceable = supportingMeasurable?.measure(textConstraints)

            val titleHeight = titlePlaceable?.height ?: 0
            val descriptionHeight = descriptionPlaceable?.height ?: 0
            val supportingHeight = supportingPlaceable?.height ?: 0

            val textBlockHeight =
                titleHeight +
                        (if (descriptionPlaceable != null) descriptionHeight + 4.dp.roundToPx() else 0) +
                        (if (supportingPlaceable != null) supportingHeight + 4.dp.roundToPx() else 0)

            val totalHeight = maxOf(
                textBlockHeight,
                startPlaceable?.height ?: 0,
                endPlaceable?.height ?: 0
            )

            layout(constraints.maxWidth, totalHeight) {
                startPlaceable?.placeRelative(
                    x = 0,
                    y = (totalHeight - startPlaceable.height) / 2
                )

                val textStartX = startWidth + startSpacerWidth
                var textVerticalPos = (totalHeight - textBlockHeight) / 2

                titlePlaceable?.placeRelative(
                    x = textStartX,
                    y = textVerticalPos
                )
                textVerticalPos += titleHeight

                if (descriptionPlaceable != null) {
                    textVerticalPos += 4.dp.roundToPx()
                    descriptionPlaceable.placeRelative(
                        x = textStartX,
                        y = textVerticalPos
                    )
                    textVerticalPos += descriptionHeight
                }

                if (supportingPlaceable != null) {
                    textVerticalPos += 4.dp.roundToPx()
                    supportingPlaceable.placeRelative(
                        x = textStartX,
                        y = textVerticalPos
                    )
                }

                endPlaceable?.placeRelative(
                    x = constraints.maxWidth - endWidth,
                    y = (totalHeight - endPlaceable.height) / 2
                )
            }
        }
    }
}
