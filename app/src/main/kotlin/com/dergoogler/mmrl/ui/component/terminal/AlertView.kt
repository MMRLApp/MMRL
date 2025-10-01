package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.AlertType
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun AlertView(block: AlertBlock) {
    val style = LocalTextStyle.current
    val colorScheme = MaterialTheme.colorScheme
    val width = LocalTerminalWidth.current

    val (color, textColor) = remember(block.type) {
        when (block.type) {
            AlertType.NOTICE -> Color(0xff2294f2) to "#2294f2"
            AlertType.WARNING -> Color.Yellow to "yellow"
            AlertType.ERROR -> Color.Red to "red"
        }
    }

    val type = remember(block.type, block.title) {
        block.title ?: when (block.type) {
            AlertType.NOTICE -> "Notice"
            AlertType.WARNING -> "Warning"
            AlertType.ERROR -> "Error"
        }
    }

    Line(
        modifier = Modifier
            .width(width)
            .background(color.copy(alpha = 0.09f)),
        index = block.lineNumber
    ) {
        BBCodeText(
            text = "[color=$textColor]$type:[/color] ${block.text}",
            style = style.copy(color = colorScheme.outline)
        )
    }
}