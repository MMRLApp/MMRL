package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun TextView(block: TextBlock) {
    Line(
        index = block.lineNumber
    ) {
        BBCodeText(
            text = block.text
        )
    }
}
