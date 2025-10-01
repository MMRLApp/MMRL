package com.dergoogler.mmrl.ui.component.dialog.dsl

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dergoogler.mmrl.ui.component.card.card
import com.dergoogler.mmrl.ui.token.applyTonalElevation

@Composable
fun DialogContainer(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    style: DialogContainerStyle = DialogContainerDefaults.style,
    contentPadding: DialogContentPadding = DialogContainerDefaults.contentPadding,
    content: @Composable DialogContainerScope.() -> Unit,
) {
    val instance = remember {
        DialogContainerScopeInstance(style, contentPadding)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Layout(
            modifier = modifier
                .border(
                    style.borderWidth,
                    style.containerOutline,
                    style.shape
                )
                .card(
                    shape = style.shape,
                    border = null,
                    backgroundColor = MaterialTheme.colorScheme.applyTonalElevation(
                        style.containerColor,
                        style.tonalElevation
                    ),
                    shadowElevation = with(LocalDensity.current) { style.tonalElevation.toPx() }
                )
                .semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                }
                .pointerInput(Unit) {},
            content = {
                instance.content()
            }
        ) { measurables, constraints ->
            val titleMeasurable = measurables.find { it.layoutId == DialogContainerSlot.TITLE }
            val contentMeasurable = measurables.find { it.layoutId == DialogContainerSlot.CONTENT }
            val buttonsMeasurable = measurables.find { it.layoutId == DialogContainerSlot.BUTTONS }

            val titlePlaceable = titleMeasurable?.measure(
                constraints.copy(minHeight = 0)
            )
            val titleHeight = titlePlaceable?.height ?: 0

            val buttonsPlaceable = buttonsMeasurable?.measure(
                constraints.copy(minHeight = 0)
            )
            val buttonsHeight = buttonsPlaceable?.height ?: 0

            val remainingHeight = constraints.maxHeight - titleHeight - buttonsHeight
            val contentConstraints = constraints.copy(
                minHeight = 0,
                maxHeight = if (remainingHeight > 0) remainingHeight else constraints.maxHeight
            )

            val contentPlaceable = contentMeasurable?.measure(contentConstraints)
            val contentHeight = contentPlaceable?.height ?: 0

            val totalHeight = titleHeight + contentHeight + buttonsHeight
            val layoutHeight = totalHeight.coerceAtMost(constraints.maxHeight)

            layout(constraints.maxWidth, layoutHeight) {
                var yPosition = 0

                titlePlaceable?.place(0, yPosition)
                yPosition += titleHeight

                contentPlaceable?.place(0, yPosition)
                yPosition += contentHeight

                buttonsPlaceable?.place(0, yPosition)
            }
        }
    }
}