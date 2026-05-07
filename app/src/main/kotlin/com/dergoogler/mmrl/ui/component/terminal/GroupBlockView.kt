package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.component.text.TextWithIcon
import com.dergoogler.mmrl.ui.component.text.TextWithIconDefaults

@Composable
fun GroupBlockView(group: GroupBlock) {
    var expanded by remember(group.initiallyExpanded) {
        mutableStateOf(group.initiallyExpanded)
    }

    val width = LocalTerminalWidth.current
    val style = LocalTextStyle.current
    val colorScheme = MaterialTheme.colorScheme

    Column {
        if (group.title != null) {
            Line(
                modifier =
                    Modifier
                        .width(width)
                        .clickable { expanded = !expanded },
                index = group.startLine,
            ) {
                val color = colorScheme.primary
                val icon =
                    remember(expanded) {
                        if (expanded) R.drawable.caret_up_filled else R.drawable.caret_down_filled
                    }

                TextWithIcon(
                    icon = icon,
                    style =
                        TextWithIconDefaults.style.copy(
                            textStyle = style.copy(color = color),
                            iconTint = color,
                        ),
                    text = group.title,
                )
            }
        }

        if (expanded) {
            group.lines.forEach { (index, line) ->
                val tabs = remember { " ".repeat(4) }

                Line(index = index) {
                    BBCodeText(text = tabs + line)
                }
            }
        }
    }
}
