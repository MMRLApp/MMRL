package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun Console(
    modifier: Modifier = Modifier,
    list: SnapshotStateList<String>,
    state: LazyListState,
    showLineNumbers: Boolean = true,
    breakList: Boolean = false,
    style: TextStyle =
        MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        ),
    textLine: @Composable (String) -> Unit = {
        BBCodeText(
            text = it,
            style = style,
            color = style.color,
        )
    },
) {
    LaunchedEffect(list.size) {
        if (list.isNotEmpty()) {
            state.animateScrollToItem(list.size - 1)
        }
    }

    SelectionContainer {
        LazyColumn(
            state = state,
            modifier =
                modifier.let {
                    if (breakList) it else it.horizontalScroll(rememberScrollState())
                },
        ) {
            itemsIndexed(list) { index, item ->
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()

                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(
                                if (isHovered) {
                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.34f)
                                } else {
                                    Color.Transparent
                                },
                            ).hoverable(interactionSource = interactionSource),
                ) {
                    DisableSelection {
                        if (showLineNumbers) {
                            Text(
                                text = "${index + 1}".padStart(4),
                                modifier =
                                    Modifier
                                        .padding(end = 8.dp)
                                        .width(40.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                style = style,
                            )
                        }
                    }

                    textLine(item)
                }
            }
        }
    }
}
